package com.example.ecommerce.catalog.product.service;

import com.example.ecommerce.catalog.category.repository.CategoryRepository;
import com.example.ecommerce.catalog.product.dto.*;
import com.example.ecommerce.catalog.product.entity.Product;
import com.example.ecommerce.catalog.product.repository.ProductRepository;
import com.example.ecommerce.common.exception.BadRequestException;
import com.example.ecommerce.common.exception.ConflictException;
import com.example.ecommerce.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(UUID categoryId, String q, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        if (minPrice != null && minPrice.signum() < 0) {
            throw new BadRequestException("minPrice must be >= 0");
        }
        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new BadRequestException("maxPrice must be >= 0");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("minPrice cannot be greater than maxPrice");
        }
        return productRepository.search(categoryId, q, minPrice, maxPrice, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID id) {
        var p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (p.getDeletedAt() != null) throw new ResourceNotFoundException("Product not found");
        return toResponse(p);
    }

    @Transactional
    public ProductResponse create(ProductCreateRequest req) {
        // SKU uniqueness (if you set unique in DB, this is still helpful for nice error)
        if (productRepository.existsBySku(req.getSku())) {
            throw new ConflictException("SKU already exists");
        }

        var category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        var p = Product.builder()
                .name(req.getName())
                .sku(req.getSku())
                .description(req.getDescription())
                .price(req.getPrice())
                .stockQuantity(req.getStockQuantity())
                .active(req.getActive() != null ? req.getActive() : true)
                .category(category)
                .build();

        return toResponse(productRepository.save(p));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductUpdateRequest req) {
        var p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        var category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setStockQuantity(req.getStockQuantity());
        p.setActive(req.getActive());
        p.setCategory(category);

        return toResponse(productRepository.save(p));
    }

    @Transactional
    public void softDelete(UUID id) {
        var p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        p.setActive(false);
        p.setDeletedAt(Instant.now());
        productRepository.save(p);
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .sku(p.getSku())
                .description(p.getDescription())
                .price(p.getPrice())
                .stockQuantity(p.getStockQuantity())
                .active(p.isActive())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .build();
    }
}
