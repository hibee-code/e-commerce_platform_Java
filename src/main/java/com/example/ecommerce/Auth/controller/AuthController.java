package com.example.ecommerce.Auth.controller;

import com.example.ecommerce.Auth.dto.AuthResponse;
import com.example.ecommerce.Auth.dto.LoginRequest;
import com.example.ecommerce.Auth.dto.RegisterRequest;
import com.example.ecommerce.Auth.service.AuthService;
import com.example.ecommerce.common.api.ApiResponse;
import com.example.ecommerce.common.exception.BadRequestException;
import com.example.ecommerce.user.entity.RoleName;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/user")
    @Operation(summary = "Register a new user account")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User registered"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        if (req.getRole() != RoleName.ROLE_USER) {
            throw new BadRequestException("Role must be ROLE_USER for this endpoint");
        }
        return ApiResponse.ok("Registered", authService.register(req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register/admin")
    @Operation(summary = "Register a new admin account")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Admin registered"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Admin already exists")
    })
    public ApiResponse<AuthResponse> registerAdmin(@Valid @RequestBody RegisterRequest req) {
        if (req.getRole() != RoleName.ROLE_ADMIN) {
            throw new BadRequestException("Role must be ROLE_ADMIN for this endpoint");
        }
        return ApiResponse.ok("Registered", authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok("Logged in", authService.login(req));
    }
}
