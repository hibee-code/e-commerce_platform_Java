package com.example.ecommerce.payment.paystack;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaystackWebhookEvent {
    private String event;
    private PaystackWebhookData data;
}
