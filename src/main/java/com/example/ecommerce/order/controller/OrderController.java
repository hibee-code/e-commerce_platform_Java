package com.example.ecommerce.order.controller;


import com.example.ecommerce.auth.security.AuthPrincipal;
import com.example.ecommerce.common.api.ApiResponse;
import com.example.ecommerce.common.util.SortUtils;
import com.example.ecommerce.order.dto.OrderResponse;
import com.example.ecommerce.order.dto.OrderSummaryResponse;
import com.example.ecommerce.order.service.OrderQueryService;
import com.example.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/user/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order checkout and history")
@SecurityRequirement(name = "bearerAuth")
@Validated
@PreAuthorize("hasRole('USER')")
public class OrderController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "status", "totalAmount");

    private final OrderService orderService;         // checkout
    private final OrderQueryService orderQueryService; // list/details

    @PostMapping("/checkout")
    @Operation(summary = "Checkout current cart and create an order")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Checkout created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ApiResponse<OrderResponse> checkout(@Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal) {
        var dto = orderService.checkout(principal.getUserId());
        return ApiResponse.ok("Checkout created", dto);
    }

    @GetMapping
    @Operation(summary = "List orders for current user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orders returned")
    })
    public ApiResponse<Page<OrderSummaryResponse>> list(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(SortUtils.parseSort(sort, ALLOWED_SORT_FIELDS)));
        return ApiResponse.ok("Orders", orderQueryService.listForUser(principal.getUserId(), pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ApiResponse<OrderResponse> details(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID id
    ) {
        return ApiResponse.ok("Order", orderQueryService.detailsForUser(principal.getUserId(), id));
    }

}
