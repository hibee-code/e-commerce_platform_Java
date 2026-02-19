package com.example.ecommerce.payment.service;

import com.example.ecommerce.common.exception.BadRequestException;
import com.example.ecommerce.common.exception.ResourceNotFoundException;
import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.entity.OrderStatus;
import com.example.ecommerce.order.repository.OrderRepository;
import com.example.ecommerce.payment.entity.Payment;
import com.example.ecommerce.payment.entity.PaymentAttempt;
import com.example.ecommerce.payment.entity.PaymentProvider;
import com.example.ecommerce.payment.entity.PaymentStatus;
import com.example.ecommerce.payment.paystack.PaystackClient;
import com.example.ecommerce.payment.paystack.PaystackInitializeRequest;
import com.example.ecommerce.payment.paystack.PaystackWebhookEvent;
import com.example.ecommerce.payment.repository.PaymentAttemptRepository;
import com.example.ecommerce.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository attemptRepository;
    private final OrderRepository orderRepository;
    private final PaystackClient paystackClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public Payment initializeForOrder(UUID userId, UUID orderId) {
        Order order = orderRepository.findByIdForUserWithPayment(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.PAID) {
            return order.getPayment();
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Order is not eligible for payment");
        }

        Payment payment = order.getPayment();
        if (payment == null) {
            payment = Payment.builder()
                    .order(order)
                    .provider(PaymentProvider.PAYSTACK)
                    .status(PaymentStatus.PENDING)
                    .reference("PAY-" + UUID.randomUUID())
                    .amount(order.getTotalAmount())
                    .build();
            payment = paymentRepository.save(payment);
            order.setPayment(payment);
        }
        if (payment.getProvider() != PaymentProvider.PAYSTACK) {
            throw new BadRequestException("Unsupported payment provider");
        }

        if (payment.getAuthorizationUrl() != null && !payment.getAuthorizationUrl().isBlank()) {
            return payment;
        }

        PaystackInitializeRequest request = new PaystackInitializeRequest();
        request.setEmail(order.getUser().getEmail());
        request.setAmount(toKobo(payment.getAmount()));
        request.setReference(payment.getReference());

        var response = paystackClient.initialize(request);
        if (response == null || !response.isStatus() || response.getData() == null) {
            throw new BadRequestException("Failed to initialize Paystack payment");
        }

        payment.setAuthorizationUrl(response.getData().getAuthorizationUrl());
        payment.setAccessCode(response.getData().getAccessCode());
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment verifyForUser(UUID userId, String reference) {
        Payment payment = paymentRepository.findByReferenceForUser(reference, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getProvider() != PaymentProvider.PAYSTACK) {
            throw new BadRequestException("Unsupported payment provider");
        }
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return payment;
        }

        var response = paystackClient.verify(reference);
        if (response == null || !response.isStatus() || response.getData() == null) {
            throw new BadRequestException("Failed to verify Paystack payment");
        }

        return applyPaystackStatus(payment, response.getData().getStatus(), response.getData().getGatewayResponse());
    }

    @Transactional
    public void handleWebhook(String signature, String payload) {
        if (!paystackClient.isValidSignature(signature, payload)) {
            throw new BadRequestException("Invalid Paystack signature");
        }
        try {
            PaystackWebhookEvent event = objectMapper.readValue(payload, PaystackWebhookEvent.class);
            if (event == null || event.getData() == null || event.getData().getReference() == null) {
                return;
            }
            Payment payment = paymentRepository.findByReference(event.getData().getReference())
                    .orElse(null);
            if (payment == null) {
                return;
            }
            applyPaystackStatus(payment, event.getData().getStatus(), event.getData().getGatewayResponse());
        } catch (Exception e) {
            throw new BadRequestException("Invalid Paystack payload");
        }
    }

    private Payment applyPaystackStatus(Payment payment, String providerStatus, String message) {
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return payment;
        }

        String normalized = providerStatus == null ? "" : providerStatus.toLowerCase(Locale.ROOT);
        PaymentStatus newStatus;
        OrderStatus orderStatus;
        String failureReason = null;

        switch (normalized) {
            case "success" -> {
                newStatus = PaymentStatus.SUCCESS;
                orderStatus = OrderStatus.PAID;
            }
            case "failed", "abandoned" -> {
                newStatus = PaymentStatus.FAILED;
                orderStatus = OrderStatus.PENDING_PAYMENT;
                failureReason = message != null ? message : "Payment failed";
            }
            default -> {
                return payment;
            }
        }

        attemptRepository.save(PaymentAttempt.builder()
                .payment(payment)
                .reference(payment.getReference())
                .status(newStatus)
                .message(message)
                .build());

        payment.setStatus(newStatus);
        payment.setFailureReason(failureReason);
        payment.getOrder().setStatus(orderStatus);

        return paymentRepository.save(payment);
    }

    private long toKobo(BigDecimal amount) {
        return amount.movePointRight(2).longValueExact();
    }
}
