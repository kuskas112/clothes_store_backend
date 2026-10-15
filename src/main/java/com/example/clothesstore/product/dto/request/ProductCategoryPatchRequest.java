package com.example.clothesstore.product.dto.request;

public record ProductCategoryPatchRequest(
        Integer sortOrder,
        Boolean isPrimary
) {}