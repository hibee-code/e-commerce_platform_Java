package com.example.ecommerce.Auth.security;

import com.example.ecommerce.user.entity.RoleName;

import java.util.UUID;

public class AuthPrincipal {

    private final UUID userId;
    private final String email;
    private final RoleName role;

    public AuthPrincipal(UUID userId, String email, RoleName role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public RoleName getRole() {
        return role;
    }
}
