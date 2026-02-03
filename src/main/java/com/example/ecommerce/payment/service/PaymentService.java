package com.example.ecommerce.payment.service;

import com.example.ecommerce.common.exception.ResourceNotFoundException;
import com.example.ecommerce.order.entity.OrderStatus;
import com.example.ecommerce.payment.entity.Payment;
import com.example.ecommerce.payment.entity.PaymentAttempt;
import com.example.ecommerce.payment.entity.PaymentStatus;
import com.example.ecommerce.payment.repository.PaymentAttemptRepository;
import com.example.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository attemptRepository;

    @Transactional
    public Payment confirm(String reference, boolean success) {
        var payment = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return payment; // idempotent
        }

        var newStatus = success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        // create attempt
        attemptRepository.save(PaymentAttempt.builder()
                .payment(payment)
                .reference(reference)
                .status(newStatus)
                .message(success ? "Simulated success" : "Simulated failure")
                .build());

        payment.setStatus(newStatus);
        payment.setFailureReason(success ? null : "Simulated failure");

        // update order
        var order = payment.getOrder();
        order.setStatus(success ? OrderStatus.PAID : OrderStatus.PENDING_PAYMENT);

        return paymentRepository.save(payment);
    }
}
