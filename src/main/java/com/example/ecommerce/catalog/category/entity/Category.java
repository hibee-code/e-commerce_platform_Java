package com.example.ecommerce.catalog.category.entity;

import com.example.ecommerce.catalog.product.entity.Product;
import com.example.ecommerce.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_categories_slug", columnNames = "slug"),
        indexes = @Index(name = "idx_categories_slug", columnList = "slug")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    // Use slug for stable URLs: "men-shoes"
    @Column(nullable = false, length = 160)
    private String slug;

    @Column(length = 500)
    private String description;

    // One-to-Many: category -> products
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();
}