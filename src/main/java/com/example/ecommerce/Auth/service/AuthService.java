package com.example.ecommerce.Auth.service;


import com.example.ecommerce.Auth.dto.AuthResponse;
import com.example.ecommerce.Auth.dto.LoginRequest;
import com.example.ecommerce.Auth.dto.RegisterRequest;
import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.common.exception.BadRequestException;
import com.example.ecommerce.common.exception.ConflictException;
import com.example.ecommerce.user.entity.RoleName;
import com.example.ecommerce.user.entity.User;
import com.example.ecommerce.user.repository.RoleRepository;
import com.example.ecommerce.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        var role = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new BadRequestException("Role not found"));

        var user = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .passwordHash(encoder.encode(req.getPassword()))
                .enabled(true)
                .build();

        user.getRoles().add(role);

        // create empty cart automatically
        var cart = Cart.builder().user(user).build();
        user.setCart(cart);

        var saved = userRepository.save(user);
        return new AuthResponse(jwtService.generate(saved, role.getName()));
    }

    public AuthResponse login(LoginRequest req) {
        var user = userRepository.findByEmailWithRoles(req.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid credentials");
        }

        RoleName role = req.getRole();
        boolean hasRole = user.getRoles().stream().anyMatch(r -> r.getName() == role);
        if (!hasRole) {
            throw new BadRequestException("Role not assigned");
        }

        return new AuthResponse(jwtService.generate(user, role));
    }
}
