package com.example.ecommerce.order.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private UUID id;
    private UUID productId;
    private String productName; // snapshot
    private String sku;         // snapshot
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;
}