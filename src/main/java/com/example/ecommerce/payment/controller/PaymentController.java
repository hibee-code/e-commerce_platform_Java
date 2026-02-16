package com.example.ecommerce.payment.controller;


import com.example.ecommerce.common.api.ApiResponse;
import com.example.ecommerce.payment.dto.PaymentResponse;
import com.example.ecommerce.payment.entity.Payment;
import com.example.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment confirmation")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    @Operation(summary = "Confirm a payment")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment processed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ApiResponse<PaymentResponse> confirm(
            @RequestParam @NotBlank String reference,
            @RequestParam boolean success
    ) {
        Payment payment = paymentService.confirm(reference, success);
        return ApiResponse.ok("Payment processed", toResponse(payment));
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .reference(payment.getReference())
                .status(payment.getStatus().name())
                .provider(payment.getProvider().name())
                .amount(payment.getAmount())
                .failureReason(payment.getFailureReason())
                .build();
    }
}
