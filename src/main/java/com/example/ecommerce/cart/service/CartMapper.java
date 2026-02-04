package com.example.ecommerce.cart.service;

import com.example.ecommerce.cart.dto.CartItemResponse;
import com.example.ecommerce.cart.dto.CartResponse;
import com.example.ecommerce.cart.entity.Cart;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(i -> {
            var p = i.getProduct();
            BigDecimal unit = p.getPrice();
            BigDecimal line = unit.multiply(BigDecimal.valueOf(i.getQuantity()));
            return CartItemResponse.builder()
                    .id(i.getId())
                    .productId(p.getId())
                    .productName(p.getName())
                    .sku(p.getSku())
                    .unitPrice(unit)
                    .quantity(i.getQuantity())
                    .lineTotal(line)
                    .build();
        }).toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .total(total)
                .build();
    }
}
