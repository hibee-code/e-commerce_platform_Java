package com.example.ecommerce.catalog.category.controller;

import com.example.ecommerce.catalog.category.dto.*;
import com.example.ecommerce.catalog.category.service.CategoryService;
import com.example.ecommerce.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<java.util.List<CategoryResponse>> list() {
        return ApiResponse.ok("Categories", categoryService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Category", categoryService.get(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest req) {
        return ApiResponse.ok("Category created", categoryService.create(req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable UUID id, @Valid @RequestBody CategoryUpdateRequest req) {
        return ApiResponse.ok("Category updated", categoryService.update(id, req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ApiResponse.ok("Category deleted", null);
    }
}
