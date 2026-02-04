package com.example.ecommerce.catalog.product.controller;

import com.example.ecommerce.catalog.product.dto.*;
import com.example.ecommerce.catalog.product.service.ProductService;
import com.example.ecommerce.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<Page<ProductResponse>> search(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseSort(sort)));
        return ApiResponse.ok("Products", productService.search(categoryId, q, minPrice, maxPrice, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Product", productService.get(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductCreateRequest req) {
        return ApiResponse.ok("Product created", productService.create(req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest req) {
        return ApiResponse.ok("Product updated", productService.update(id, req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        productService.softDelete(id);
        return ApiResponse.ok("Product deleted", null);
    }

    private Sort.Order parseSort(String sort) {
        // supports: "price,asc" or "createdAt,desc"
        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction = (parts.length > 1 && parts[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return new Sort.Order(direction, property);
    }
}

