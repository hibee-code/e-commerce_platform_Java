package com.example.ecommerce.Auth.service;


import com.example.ecommerce.user.entity.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private final Key key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expirationMinutes:1440}") long expMinutes
    ) {
        // fail fast: clear error for misconfig
        if (secret == null || secret.trim().length() < 32) {
            throw new IllegalStateException(
                    "JWT secret missing or too short (min 32 chars). Set APP_JWT_SECRET / app.jwt.secret."
            );
        }
        if (expMinutes <= 0) {
            throw new IllegalStateException("JWT expirationMinutes must be > 0");
        }

        try {
            this.key = Keys.hmacShaKeyFor(secret.trim().getBytes(StandardCharsets.UTF_8));
        } catch (WeakKeyException e) {
            throw new IllegalStateException("JWT secret is too weak for HS256 (use 32+ chars).", e);
        }

        this.expirationMs = expMinutes * 60_000L;
    }

    public String generate(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return parser().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean isValid(String token) {
        try {
            parser().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private JwtParser parser() {
        return Jwts.parserBuilder().setSigningKey(key).build();
    }
}