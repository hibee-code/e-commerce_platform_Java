package com.example.ecommerce.cart.controller;

import com.example.ecommerce.Auth.security.AuthUser;
import com.example.ecommerce.cart.dto.AddToCartRequest;
import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<Cart> get(@AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok("Cart", cartService.get(user.getDomainUser().getId()));
    }

    @PostMapping("/items")
    public ApiResponse<Cart> add(@AuthenticationPrincipal AuthUser user, @RequestBody AddToCartRequest req) {
        return ApiResponse.ok("Item added", cartService.addItem(user.getDomainUser().getId(), req));
    }

    @PatchMapping("/items/{itemId}")
    public ApiResponse<Cart> updateQty(@AuthenticationPrincipal AuthUser user,
                                       @PathVariable UUID itemId,
                                       @RequestParam int qty) {
        return ApiResponse.ok("Quantity updated", cartService.updateQty(user.getDomainUser().getId(), itemId, qty));
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<Cart> remove(@AuthenticationPrincipal AuthUser user, @PathVariable UUID itemId) {
        return ApiResponse.ok("Item removed", cartService.removeItem(user.getDomainUser().getId(), itemId));
    }

    @DeleteMapping("/clear")
    public ApiResponse<Cart> clear(@AuthenticationPrincipal AuthUser user) {
        return ApiResponse.ok("Cart cleared", cartService.clear(user.getDomainUser().getId()));
    }
}