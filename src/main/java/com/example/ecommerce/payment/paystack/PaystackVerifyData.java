package com.example.ecommerce.payment.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaystackVerifyData {
    private String status;
    private String reference;
    @JsonProperty("gateway_response")
    private String gatewayResponse;
    private long amount;
    private String currency;
}
