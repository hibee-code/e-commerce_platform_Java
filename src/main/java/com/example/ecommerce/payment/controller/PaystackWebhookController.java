package com.example.ecommerce.payment.controller;

import com.example.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Paystack webhooks")
public class PaystackWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/webhook")
    @Operation(summary = "Paystack webhook callback")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Webhook processed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(name = "x-paystack-signature", required = false) String signature,
            @RequestBody String payload
    ) {
        paymentService.handleWebhook(signature, payload);
        return ResponseEntity.ok("OK");
    }
}
