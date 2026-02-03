package com.example.ecommerce.order.controller;


import com.example.ecommerce.Auth.security.AuthUser;
import com.example.ecommerce.common.api.ApiResponse;
import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ApiResponse<Order> checkout(@AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok("Checkout created", orderService.checkout(user.getDomainUser().getId()));
    }
}
