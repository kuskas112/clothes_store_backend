package com.example.clothesstore.product.dto.request;

public record ProductImagePatchRequest(
        String altText,
        Integer sortOrder,
        Boolean isMain
) {}