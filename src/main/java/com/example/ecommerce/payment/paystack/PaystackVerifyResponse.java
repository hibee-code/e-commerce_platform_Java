package com.example.ecommerce.payment.paystack;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaystackVerifyResponse {
    private boolean status;
    private String message;
    private PaystackVerifyData data;
}
