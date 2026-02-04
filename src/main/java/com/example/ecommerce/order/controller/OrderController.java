package com.example.ecommerce.order.controller;


import com.example.ecommerce.Auth.security.AuthUser;
import com.example.ecommerce.common.api.ApiResponse;
import com.example.ecommerce.order.dto.OrderResponse;
import com.example.ecommerce.order.dto.OrderSummaryResponse;
import com.example.ecommerce.order.entity.Order;
import com.example.ecommerce.order.service.OrderQueryService;
import com.example.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;         // checkout
    private final OrderQueryService orderQueryService; // list/details

    @PostMapping("/checkout")
    public ApiResponse<OrderResponse> checkout(@AuthenticationPrincipal AuthUser user) {
        var order = orderService.checkout(user.getDomainUser().getId());
        // OrderService currently returns Order entity; map to DTO via query for detail
        // Faster: create a mapper in OrderService; simplest: return detail by ID
        var dto = orderQueryService.detailsForUser(user.getDomainUser().getId(), order.getId());
        return ApiResponse.ok("Checkout created", dto);
    }

    @GetMapping
    public ApiResponse<Page<OrderSummaryResponse>> list(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseSort(sort)));
        return ApiResponse.ok("Orders", orderQueryService.listForUser(user.getDomainUser().getId(), pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> details(@AuthenticationPrincipal AuthUser user, @PathVariable UUID id) {
        return ApiResponse.ok("Order", orderQueryService.detailsForUser(user.getDomainUser().getId(), id));
    }

    private Sort.Order parseSort(String sort) {
        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction = (parts.length > 1 && parts[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return new Sort.Order(direction, property);
    }
}
