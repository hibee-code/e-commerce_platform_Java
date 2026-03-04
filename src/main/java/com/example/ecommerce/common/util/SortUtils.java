package com.example.ecommerce.common.util;

import com.example.ecommerce.common.exception.BadRequestException;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class SortUtils {

    private SortUtils() {
    }

    public static Sort.Order parseSort(String sort, Set<String> allowedFields) {
        if (sort == null || sort.isBlank()) {
            throw new BadRequestException("Sort is required");
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        if (!allowedFields.contains(property)) {
            throw new BadRequestException("Invalid sort field: " + property);
        }
        Sort.Direction direction = (parts.length > 1 && parts[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return new Sort.Order(direction, property);
    }
}
