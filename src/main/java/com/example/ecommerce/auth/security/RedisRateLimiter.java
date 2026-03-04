package com.example.ecommerce.auth.security;

import com.example.ecommerce.config.RateLimitProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiter.class);

    private static final String SCRIPT = """
        local key = KEYS[1]
        local capacity = tonumber(ARGV[1])
        local refillTokens = tonumber(ARGV[2])
        local refillMillis = tonumber(ARGV[3])
        local now = tonumber(ARGV[4])

        local data = redis.call('HMGET', key, 'tokens', 'ts')
        local tokens = tonumber(data[1])
        local ts = tonumber(data[2])
        if tokens == nil then
          tokens = capacity
          ts = now
        end

        if now > ts then
          local delta = now - ts
          local refill = (delta / refillMillis) * refillTokens
          if refill > 0 then
            tokens = math.min(capacity, tokens + refill)
            ts = now
          end
        end

        local allowed = 0
        local retryAfter = 0
        if tokens >= 1 then
          tokens = tokens - 1
          allowed = 1
        else
          if refillTokens > 0 then
            local needed = 1 - tokens
            local waitMillis = math.ceil((needed / refillTokens) * refillMillis)
            retryAfter = math.ceil(waitMillis / 1000)
          else
            retryAfter = math.ceil(refillMillis / 1000)
          end
        end

        redis.call('HMSET', key, 'tokens', tokens, 'ts', ts)
        local ttlSeconds = math.ceil((2 * refillMillis) / 1000)
        redis.call('EXPIRE', key, ttlSeconds)

        return {allowed, retryAfter}
        """;

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;
    private final DefaultRedisScript<List> script = new DefaultRedisScript<>(SCRIPT, List.class);

    public RateLimitResult tryConsume(String key) {
        RateLimitProperties.Auth auth = properties.getAuth();
        String redisKey = auth.getRedisKeyPrefix() + key;

        long capacity = Math.max(1, auth.getCapacity());
        long refillTokens = Math.max(1, auth.getRefillTokens());
        long refillMillis = Math.max(1, safeToMillis(auth.getRefillDuration()));
        long now = System.currentTimeMillis();

        try {
            List<?> result = redisTemplate.execute(
                    script,
                    List.of(redisKey),
                    String.valueOf(capacity),
                    String.valueOf(refillTokens),
                    String.valueOf(refillMillis),
                    String.valueOf(now)
            );

            long allowed = getLong(result, 0);
            long retryAfter = getLong(result, 1);
            return new RateLimitResult(allowed == 1L, Math.max(0L, retryAfter));
        } catch (Exception ex) {
            log.warn("Redis rate limiter failed; allowing request. error={}", ex.getMessage());
            return new RateLimitResult(true, 0);
        }
    }

    private long safeToMillis(Duration duration) {
        if (duration == null) {
            return Duration.ofMinutes(1).toMillis();
        }
        long millis = duration.toMillis();
        return millis > 0 ? millis : Duration.ofMinutes(1).toMillis();
    }

    private long getLong(List<?> result, int index) {
        if (result == null || result.size() <= index || result.get(index) == null) {
            return 0L;
        }
        Object value = result.get(index);
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
