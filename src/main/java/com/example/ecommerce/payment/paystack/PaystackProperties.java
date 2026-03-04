package com.example.ecommerce.payment.paystack;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "paystack")
public class PaystackProperties {

    @NotBlank
    private String secretKey;

    private String baseUrl = "https://api.paystack.co";

    private String callbackUrl = "";

    private Duration connectTimeout = Duration.ofSeconds(2);

    private Duration readTimeout = Duration.ofSeconds(5);

    private final Retry retry = new Retry();

    @Getter
    @Setter
    public static class Retry {
        private int maxAttempts = 3;
        private Duration backoff = Duration.ofMillis(500);
        private Duration maxBackoff = Duration.ofSeconds(2);
        private double jitter = 0.2d;
    }
}
