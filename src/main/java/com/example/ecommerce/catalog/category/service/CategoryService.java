package com.example.ecommerce.catalog.category.service;

import com.example.ecommerce.catalog.category.dto.*;
import com.example.ecommerce.catalog.category.entity.Category;
import com.example.ecommerce.catalog.category.repository.CategoryRepository;
import com.example.ecommerce.common.exception.ConflictException;
import com.example.ecommerce.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public java.util.List<CategoryResponse> list() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(UUID id) {
        return toResponse(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found")));
    }

    @Transactional
    public CategoryResponse create(CategoryCreateRequest req) {
        if (categoryRepository.existsBySlug(req.getSlug())) {
            throw new ConflictException("Category slug already exists");
        }
        var c = Category.builder()
                .name(req.getName())
                .slug(req.getSlug())
                .description(req.getDescription())
                .build();
        return toResponse(categoryRepository.save(c));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryUpdateRequest req) {
        var c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // if slug changes, ensure unique
        if (!c.getSlug().equals(req.getSlug()) && categoryRepository.existsBySlug(req.getSlug())) {
            throw new ConflictException("Category slug already exists");
        }

        c.setName(req.getName());
        c.setSlug(req.getSlug());
        c.setDescription(req.getDescription());
        return toResponse(categoryRepository.save(c));
    }

    @Transactional
    public void delete(UUID id) {
        var c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        categoryRepository.delete(c);
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .build();
    }
}
