package com.example.ecommerce.catalog.category.dto;

import lombok.*;

import java.util.UUID;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
}
