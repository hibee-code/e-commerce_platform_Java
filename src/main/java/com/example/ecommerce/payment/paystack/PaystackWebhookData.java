package com.example.ecommerce.payment.paystack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaystackWebhookData {
    private String status;
    private String reference;
    @JsonProperty("gateway_response")
    private String gatewayResponse;
}
