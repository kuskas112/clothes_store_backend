package com.example.clothesstore.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String slug,
        @Size(max = 500) String description,
        Long parentId,
        Integer sortOrder
) {}