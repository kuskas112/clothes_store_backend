package com.example.clothesstore.product.dto.request;

import jakarta.validation.constraints.NotNull;

public record ProductImageRequest(
        @NotNull Long imageId,
        String altText,
        Integer sortOrder,
        Boolean isMain
) {}