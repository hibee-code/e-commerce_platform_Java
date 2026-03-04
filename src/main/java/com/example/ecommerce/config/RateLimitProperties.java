package com.example.ecommerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private final Auth auth = new Auth();

    @Getter
    @Setter
    public static class Auth {
        private boolean enabled = true;
        private boolean redisEnabled = false;
        private String redisKeyPrefix = "rate:auth:";
        private int capacity = 5;
        private int refillTokens = 5;
        private Duration refillDuration = Duration.ofMinutes(1);
        private int maxEntries = 10_000;
    }
}
