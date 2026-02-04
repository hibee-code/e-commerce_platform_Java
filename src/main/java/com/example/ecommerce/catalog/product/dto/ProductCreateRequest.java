package com.example.ecommerce.catalog.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProductCreateRequest {
    @NotBlank private String name;
    @NotBlank
    private String sku;
    private String description;

    @NotNull @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal price;

    @NotNull @Min(0)
    private Integer stockQuantity;

    @NotNull
    private UUID categoryId;

    private Boolean active = true;
}