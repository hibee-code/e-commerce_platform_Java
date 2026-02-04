package com.example.ecommerce.cart.controller;

import com.example.ecommerce.Auth.security.AuthUser;
import com.example.ecommerce.cart.dto.AddToCartRequest;
import com.example.ecommerce.cart.dto.CartResponse;
import com.example.ecommerce.cart.entity.Cart;
import com.example.ecommerce.cart.service.CartMapper;
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
    private final CartMapper cartMapper;

    @GetMapping
    public ApiResponse<CartResponse> get(@AuthenticationPrincipal AuthUser user) {
        var cart = cartService.get(user.getDomainUser().getId());
        return ApiResponse.ok("Cart", cartMapper.toResponse(cart));
    }

    @PostMapping("/items")
    public ApiResponse<CartResponse> add(@AuthenticationPrincipal AuthUser user, @RequestBody AddToCartRequest req) {
        var cart = cartService.addItem(user.getDomainUser().getId(), req);
        return ApiResponse.ok("Item added", cartMapper.toResponse(cart));
    }

    @PatchMapping("/items/{itemId}")
    public ApiResponse<CartResponse> updateQty(@AuthenticationPrincipal AuthUser user,
                                               @PathVariable UUID itemId,
                                               @RequestParam int qty) {
        var cart = cartService.updateQty(user.getDomainUser().getId(), itemId, qty);
        return ApiResponse.ok("Quantity updated", cartMapper.toResponse(cart));
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<CartResponse> remove(@AuthenticationPrincipal AuthUser user, @PathVariable UUID itemId) {
        var cart = cartService.removeItem(user.getDomainUser().getId(), itemId);
        return ApiResponse.ok("Item removed", cartMapper.toResponse(cart));
    }

    @DeleteMapping("/clear")
    public ApiResponse<CartResponse> clear(@AuthenticationPrincipal AuthUser user) {
        var cart = cartService.clear(user.getDomainUser().getId());
        return ApiResponse.ok("Cart cleared", cartMapper.toResponse(cart));
    }
}