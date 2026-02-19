package com.example.ecommerce.payment.paystack;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaystackInitializeResponse {
    private boolean status;
    private String message;
    private PaystackInitializeData data;
}
