package com.example.ecommerce.cart.controller;

import com.example.ecommerce.Auth.security.AuthPrincipal;
import com.example.ecommerce.cart.dto.AddToCartRequest;
import com.example.ecommerce.cart.dto.CartResponse;
import com.example.ecommerce.cart.service.CartMapper;
import com.example.ecommerce.cart.service.CartService;
import com.example.ecommerce.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart operations")
@SecurityRequirement(name = "bearerAuth")
@Validated
@PreAuthorize("hasRole('USER')")
public class CartController {
    private final CartService cartService;
    private final CartMapper cartMapper;

    @GetMapping
    @Operation(summary = "Get current user's cart")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart returned")
    })
    public ApiResponse<CartResponse> get(@Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal) {
        var cart = cartService.get(principal.getUserId());
        return ApiResponse.ok("Cart", cartMapper.toResponse(cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item added"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ApiResponse<CartResponse> add(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody AddToCartRequest req
    ) {
        var cart = cartService.addItem(principal.getUserId(), req);
        return ApiResponse.ok("Item added", cartMapper.toResponse(cart));
    }

    @PatchMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Quantity updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ApiResponse<CartResponse> updateQty(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID itemId,
            @RequestParam @Min(1) int qty
    ) {
        var cart = cartService.updateQty(principal.getUserId(), itemId, qty);
        return ApiResponse.ok("Quantity updated", cartMapper.toResponse(cart));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item removed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ApiResponse<CartResponse> remove(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID itemId
    ) {
        var cart = cartService.removeItem(principal.getUserId(), itemId);
        return ApiResponse.ok("Item removed", cartMapper.toResponse(cart));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear the cart")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart cleared")
    })
    public ApiResponse<CartResponse> clear(@Parameter(hidden = true) @AuthenticationPrincipal AuthPrincipal principal) {
        var cart = cartService.clear(principal.getUserId());
        return ApiResponse.ok("Cart cleared", cartMapper.toResponse(cart));
    }
}
