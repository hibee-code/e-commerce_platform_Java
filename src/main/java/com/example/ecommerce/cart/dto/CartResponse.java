package com.example.ecommerce.cart.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private UUID cartId;
    private UUID userId;
    private List<CartItemResponse> items;
    private BigDecimal total;
}