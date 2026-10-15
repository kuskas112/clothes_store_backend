package com.example.clothesstore.category.dto;

public record CategoryDto(
        Long id,
        String name,
        String slug,
        String description,
        Long parentId,
        Integer sortOrder,
        Boolean active
) {}