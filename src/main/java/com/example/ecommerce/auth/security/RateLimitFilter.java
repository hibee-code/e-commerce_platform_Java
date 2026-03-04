package com.example.ecommerce.auth.security;

import com.example.ecommerce.common.api.ApiResponse;
import com.example.ecommerce.common.util.RequestUtils;
import com.example.ecommerce.config.RateLimitProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<RedisRateLimiter> redisRateLimiterProvider;

    private final Map<String, BucketState> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (!properties.getAuth().isEnabled() || !isAuthEndpoint(request)) {
            chain.doFilter(request, response);
            return;
        }

        String key = clientKey(request);
        RateLimitResult result = tryConsume(key);
        if (result.allowed()) {
            chain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds = Math.max(1, result.retryAfterSeconds());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.error("Too many requests"));
    }

    private boolean isAuthEndpoint(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/login")
                || path.startsWith("/api/v1/auth/register")
                || path.startsWith("/api/v1/auth/setup");
    }

    private String clientKey(HttpServletRequest request) {
        return RequestUtils.clientIp(request);
    }

    private RateLimitResult tryConsume(String key) {
        if (properties.getAuth().isRedisEnabled()) {
            RedisRateLimiter redisRateLimiter = redisRateLimiterProvider.getIfAvailable();
            if (redisRateLimiter != null) {
                return redisRateLimiter.tryConsume(key);
            }
        }

        BucketState bucket = buckets.computeIfAbsent(key, k -> new BucketState(
                properties.getAuth().getCapacity(),
                properties.getAuth().getRefillTokens(),
                properties.getAuth().getRefillDuration()
        ));

        if (buckets.size() > properties.getAuth().getMaxEntries()) {
            buckets.clear();
        }

        if (bucket.tryConsume()) {
            return new RateLimitResult(true, 0);
        }

        long retryAfterSeconds = Math.max(1, bucket.secondsUntilNextRefill());
        return new RateLimitResult(false, retryAfterSeconds);
    }

    private static final class BucketState {
        private final int capacity;
        private final int refillTokens;
        private final long refillNanos;

        private long tokens;
        private long lastRefillNanos;

        private BucketState(int capacity, int refillTokens, Duration refillDuration) {
            this.capacity = Math.max(1, capacity);
            this.refillTokens = Math.max(1, refillTokens);
            this.refillNanos = Math.max(1L, refillDuration.toNanos());
            this.tokens = this.capacity;
            this.lastRefillNanos = System.nanoTime();
        }

        private synchronized boolean tryConsume() {
            refill();
            if (tokens <= 0) {
                return false;
            }
            tokens--;
            return true;
        }

        private synchronized long secondsUntilNextRefill() {
            long now = System.nanoTime();
            long elapsed = now - lastRefillNanos;
            long remaining = refillNanos - elapsed;
            if (remaining <= 0) {
                return 0;
            }
            return Duration.ofNanos(remaining).toSeconds();
        }

        private void refill() {
            long now = System.nanoTime();
            long elapsed = now - lastRefillNanos;
            if (elapsed < refillNanos) {
                return;
            }
            long cycles = elapsed / refillNanos;
            long add = cycles * refillTokens;
            tokens = Math.min(capacity, tokens + add);
            lastRefillNanos = now;
        }
    }
}
