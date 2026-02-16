package com.example.ecommerce.catalog.product.entity;

import com.example.ecommerce.catalog.category.entity.Category;
import com.example.ecommerce.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products",
        indexes = {
                @Index(name = "idx_products_name", columnList = "name"),
                @Index(name = "idx_products_category", columnList = "category_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, unique = true, length = 80)
    private String sku;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(nullable = false)
    private boolean active = true;

    // Many-to-One: product -> category
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_products_category"))
    private Category category;

    public boolean isDeleted() {
        return getDeletedAt() != null;
    }
}
