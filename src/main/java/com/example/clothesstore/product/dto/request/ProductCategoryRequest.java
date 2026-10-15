package com.example.clothesstore.product.dto.request;

import jakarta.validation.constraints.NotNull;

public record ProductCategoryRequest(
        @NotNull Long categoryId,
        Integer sortOrder,
        Boolean isPrimary
) {}