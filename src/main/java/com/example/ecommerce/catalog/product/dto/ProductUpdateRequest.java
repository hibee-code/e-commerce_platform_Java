package com.example.ecommerce.catalog.product.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
public class ProductUpdateRequest {
    @NotBlank private String name;
    private String description;

    @NotNull @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal price;

    @NotNull @Min(0)
    private Integer stockQuantity;

    @NotNull
    private UUID categoryId;

    @NotNull
    private Boolean active;
}