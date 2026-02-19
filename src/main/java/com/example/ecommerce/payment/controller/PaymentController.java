package com.example.ecommerce.payment.controller;


import com.example.ecommerce.Auth.security.AuthPrincipal;
import com.example.ecommerce.common.api.ApiResponse;
import com.example.ecommerce.payment.dto.PaymentInitializeRequest;
import com.example.ecommerce.payment.dto.PaymentResponse;
import com.example.ecommerce.payment.entity.Payment;
import com.example.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment initialization and verification")
@SecurityRequirement(name = "bearerAuth")
@Validated
@PreAuthorize("hasRole('USER')")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initialize")
    @Operation(summary = "Initialize a Paystack payment")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment initialized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ApiResponse<PaymentResponse> initialize(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody PaymentInitializeRequest request
    ) {
        Payment payment = paymentService.initializeForOrder(principal.getUserId(), request.getOrderId());
        return ApiResponse.ok("Payment initialized", toResponse(payment));
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify a Paystack payment")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment verified"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ApiResponse<PaymentResponse> verify(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam @NotBlank String reference
    ) {
        Payment payment = paymentService.verifyForUser(principal.getUserId(), reference);
        return ApiResponse.ok("Payment verified", toResponse(payment));
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .reference(payment.getReference())
                .status(payment.getStatus().name())
                .provider(payment.getProvider().name())
                .amount(payment.getAmount())
                .authorizationUrl(payment.getAuthorizationUrl())
                .accessCode(payment.getAccessCode())
                .failureReason(payment.getFailureReason())
                .build();
    }
}
