package com.example.ecommerce.Auth.controller;

import com.example.ecommerce.Auth.dto.AuthResponse;
import com.example.ecommerce.Auth.dto.RegisterRequest;
import com.example.ecommerce.Auth.service.AuthService;
import com.example.ecommerce.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/setup")
@RequiredArgsConstructor
@Tag(name = "Auth Setup", description = "One-time admin setup")
@ConditionalOnProperty(prefix = "app.setup.admin", name = "enabled", havingValue = "true")
public class AuthSetupController {

    private final AuthService authService;

    @PostMapping("/admin")
    @Operation(summary = "Register the first admin account (one-time)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Admin registered"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Admin already exists")
    })
    public ApiResponse<AuthResponse> setupAdmin(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok("Admin registered", authService.registerFirstAdmin(req));
    }
}
