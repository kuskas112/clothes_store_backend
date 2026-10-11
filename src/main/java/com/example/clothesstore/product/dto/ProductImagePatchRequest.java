package com.example.clothesstore.product.dto;

public record ProductImagePatchRequest(
        String altText,
        Integer sortOrder,
        Boolean isMain
) {}