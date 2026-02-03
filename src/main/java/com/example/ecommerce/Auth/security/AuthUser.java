package com.example.ecommerce.Auth.security;

import com.example.ecommerce.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.stream.Collectors;

public class AuthUser implements UserDetails {

    private final User user;

    private AuthUser(User user) { this.user = user; }

    public static AuthUser from(User user) { return new AuthUser(user); }

    public User getDomainUser() { return user; }

    @Override public String getUsername() { return user.getEmail(); }
    @Override public String getPassword() { return user.getPasswordHash(); }
    @Override public boolean isEnabled() { return user.isEnabled(); }

    @Override
    public java.util.Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getName().name()))
                .collect(Collectors.toSet());
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
