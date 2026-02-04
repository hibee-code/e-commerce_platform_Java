package com.example.ecommerce.catalog.product.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private UUID id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private boolean active;

    private UUID categoryId;
    private String categoryName;
}
