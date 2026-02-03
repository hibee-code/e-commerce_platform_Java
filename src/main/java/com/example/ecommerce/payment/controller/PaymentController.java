package com.example.ecommerce.payment.controller;


import com.example.ecommerce.common.api.ApiResponse;
import com.example.ecommerce.payment.entity.Payment;
import com.example.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ApiResponse<Payment> confirm(@RequestParam String reference, @RequestParam boolean success) {
        return ApiResponse.ok("Payment processed", paymentService.confirm(reference, success));
    }
}
