package com.example.ecommerce.catalog.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CategoryUpdateRequest {
    @NotBlank private String name;
    @NotBlank private String slug;
    private String description;
}
