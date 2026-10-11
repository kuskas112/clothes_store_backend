package com.example.clothesstore.product.dto;

import com.example.clothesstore.image.ImageResponse;

public record ProductImageDto(
        Long id,
        Integer sortOrder,
        Boolean isMain,
        ImageResponse image
) {
}