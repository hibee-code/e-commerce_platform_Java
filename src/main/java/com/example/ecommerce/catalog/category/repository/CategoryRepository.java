package com.example.ecommerce.catalog.category.repository;

import com.example.ecommerce.catalog.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    // Used by create() in CategoryService
    boolean existsBySlug(String slug);

    // Useful for read endpoints and validations
    Optional<Category> findBySlug(String slug);

    // Best-practice helper for update(): check slug uniqueness excluding the current category
    @Query("""
        SELECT COUNT(c) > 0
        FROM Category c
        WHERE c.slug = :slug
          AND c.id <> :id
    """)
    boolean existsBySlugAndIdNot(@Param("slug") String slug, @Param("id") UUID id);
}