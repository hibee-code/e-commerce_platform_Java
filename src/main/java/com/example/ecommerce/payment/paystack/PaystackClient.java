package com.example.ecommerce.payment.paystack;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Service
public class PaystackClient {

    private final RestClient restClient;
    private final String secretKey;
    private final String callbackUrl;
    private final PaystackProperties.Retry retry;

    public PaystackClient(
            RestClient.Builder builder,
            PaystackProperties properties
    ) {
        if (properties.getSecretKey() == null || properties.getSecretKey().isBlank()) {
            throw new IllegalStateException("Paystack secret key is missing. Set PAYSTACK_SECRET_KEY.");
        }
        this.secretKey = properties.getSecretKey();
        this.callbackUrl = properties.getCallbackUrl();
        this.retry = properties.getRetry();
        this.restClient = builder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getSecretKey())
                .build();
    }

    public PaystackInitializeResponse initialize(PaystackInitializeRequest request) {
        if (request.getCallbackUrl() == null || request.getCallbackUrl().isBlank()) {
            request.setCallbackUrl(callbackUrl);
        }
        return executeWithRetry(() -> restClient.post()
                .uri("/transaction/initialize")
                .body(request)
                .retrieve()
                .body(PaystackInitializeResponse.class));
    }

    public PaystackVerifyResponse verify(String reference) {
        return executeWithRetry(() -> restClient.get()
                .uri("/transaction/verify/{reference}", reference)
                .retrieve()
                .body(PaystackVerifyResponse.class));
    }

    public boolean isValidSignature(String signature, String payload) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        byte[] provided = parseHex(signature.trim());
        if (provided == null) {
            return false;
        }
        byte[] expected = hmacSHA512(payload, secretKey);
        return MessageDigest.isEqual(expected, provided);
    }

    private byte[] hmacSHA512(String payload, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to compute Paystack signature", e);
        }
    }

    private byte[] parseHex(String value) {
        try {
            return HexFormat.of().parseHex(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private <T> T executeWithRetry(Supplier<T> action) {
        int maxAttempts = Math.max(1, retry.getMaxAttempts());
        Duration backoff = retry.getBackoff() == null ? Duration.ofMillis(500) : retry.getBackoff();
        Duration maxBackoff = retry.getMaxBackoff() == null ? Duration.ofSeconds(2) : retry.getMaxBackoff();
        double jitter = Math.max(0d, retry.getJitter());

        RestClientException last = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (RestClientResponseException ex) {
                if (!isRetryableStatus(ex.getStatusCode().value()) || attempt == maxAttempts) {
                    throw ex;
                }
                last = ex;
            } catch (RestClientException ex) {
                if (attempt == maxAttempts) {
                    throw ex;
                }
                last = ex;
            }

            sleepWithBackoff(backoff, maxBackoff, jitter, attempt);
        }

        if (last != null) {
            throw last;
        }
        throw new IllegalStateException("Paystack retry failed without exception");
    }

    private boolean isRetryableStatus(int status) {
        return status == 429 || (status >= 500 && status < 600);
    }

    private void sleepWithBackoff(Duration base, Duration max, double jitter, int attempt) {
        long baseMs = Math.max(1L, base.toMillis());
        long maxMs = Math.max(baseMs, max.toMillis());
        long exp = Math.min(maxMs, baseMs * (1L << Math.max(0, attempt - 1)));
        double jitterFactor = 1d;
        if (jitter > 0d) {
            double delta = jitter * ThreadLocalRandom.current().nextDouble();
            jitterFactor = ThreadLocalRandom.current().nextBoolean() ? (1d + delta) : (1d - delta);
        }
        long sleepMs = Math.max(1L, Math.round(exp * jitterFactor));

        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Paystack retry interrupted", ie);
        }
    }
}
