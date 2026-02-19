package com.example.ecommerce.payment.paystack;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

@Service
public class PaystackClient {

    private final RestClient restClient;
    private final String secretKey;
    private final String callbackUrl;

    public PaystackClient(
            RestClient.Builder builder,
            @Value("${paystack.secret-key}") String secretKey,
            @Value("${paystack.base-url:https://api.paystack.co}") String baseUrl,
            @Value("${paystack.callback-url:}") String callbackUrl
    ) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Paystack secret key is missing. Set PAYSTACK_SECRET_KEY.");
        }
        this.secretKey = secretKey;
        this.callbackUrl = callbackUrl;
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .build();
    }

    public PaystackInitializeResponse initialize(PaystackInitializeRequest request) {
        if (request.getCallbackUrl() == null || request.getCallbackUrl().isBlank()) {
            request.setCallbackUrl(callbackUrl);
        }
        return restClient.post()
                .uri("/transaction/initialize")
                .body(request)
                .retrieve()
                .body(PaystackInitializeResponse.class);
    }

    public PaystackVerifyResponse verify(String reference) {
        return restClient.get()
                .uri("/transaction/verify/{reference}", reference)
                .retrieve()
                .body(PaystackVerifyResponse.class);
    }

    public boolean isValidSignature(String signature, String payload) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        return signature.equalsIgnoreCase(hmacSHA512(payload, secretKey));
    }

    private String hmacSHA512(String payload, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to compute Paystack signature", e);
        }
    }
}
