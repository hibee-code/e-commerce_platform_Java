package com.example.ecommerce.payment.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private String reference;
    private String status;
    private String provider;
    private BigDecimal amount;
    private String authorizationUrl;
    private String accessCode;
    private String failureReason;
}
