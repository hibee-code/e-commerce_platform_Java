package com.example.ecommerce.catalog.product.controller;

import com.example.ecommerce.catalog.product.dto.*;
import com.example.ecommerce.catalog.product.service.ProductService;
import com.example.ecommerce.common.api.ApiResponse;
import com.example.ecommerce.common.exception.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog")
@SecurityRequirement(name = "bearerAuth")
@Validated
@PreAuthorize("hasRole('USER')")
public class ProductController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "price", "name");

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Search products")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Products returned")
    })
    public ApiResponse<Page<ProductResponse>> search(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @DecimalMin(value = "0.00", inclusive = true) BigDecimal minPrice,
            @RequestParam(required = false) @DecimalMin(value = "0.00", inclusive = true) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size,
            @Parameter(description = "Sort format: field,dir (e.g. createdAt,desc)")
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseSort(sort)));
        return ApiResponse.ok("Products", productService.search(categoryId, q, minPrice, maxPrice, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Product returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ApiResponse<ProductResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok("Product", productService.get(id));
    }

    private Sort.Order parseSort(String sort) {
        // supports: "price,asc" or "createdAt,desc"
        if (sort == null || sort.isBlank()) {
            throw new BadRequestException("Sort is required");
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        if (!ALLOWED_SORT_FIELDS.contains(property)) {
            throw new BadRequestException("Invalid sort field: " + property);
        }
        Sort.Direction direction = (parts.length > 1 && parts[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return new Sort.Order(direction, property);
    }
}
