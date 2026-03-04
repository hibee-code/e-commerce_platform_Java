package com.example.ecommerce.payment.service;

import com.example.ecommerce.payment.dto.PaymentResponse;
import com.example.ecommerce.payment.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .reference(payment.getReference())
                .status(payment.getStatus().name())
                .provider(payment.getProvider().name())
                .amount(payment.getAmount())
                .authorizationUrl(payment.getAuthorizationUrl())
                .accessCode(payment.getAccessCode())
                .failureReason(payment.getFailureReason())
                .build();
    }
}
