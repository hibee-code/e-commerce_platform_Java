package com.example.ecommerce.cart.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private String sku;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;
}
