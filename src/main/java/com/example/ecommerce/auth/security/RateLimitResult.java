package com.example.ecommerce.auth.security;

public record RateLimitResult(boolean allowed, long retryAfterSeconds) {}
