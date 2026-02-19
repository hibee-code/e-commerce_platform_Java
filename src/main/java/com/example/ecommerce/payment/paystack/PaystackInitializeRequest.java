package com.example.ecommerce.payment.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaystackInitializeRequest {
    private String email;
    private long amount;
    private String reference;
    @JsonProperty("callback_url")
    private String callbackUrl;
}
