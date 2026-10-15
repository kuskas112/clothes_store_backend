package com.example.clothesstore.category.dto.request;

import jakarta.validation.constraints.Size;

public record CategoryPatchRequest(
        @Size(max = 100) String name,
        @Size(max = 100) String slug,
        @Size(max = 500) String description,
        Long parentId,
        Integer sortOrder,
        Boolean active
) {}