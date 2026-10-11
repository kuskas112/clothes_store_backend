package com.example.clothesstore.product.dto;

import com.example.clothesstore.product.Gender;
import com.example.clothesstore.product.Season;

import java.math.BigDecimal;
import java.util.List;

public record ProductDto(
        Integer id,
        String name,
        Gender gender,
        Season season,
        BigDecimal basePrice,
        String description,
        List<ProductVariantDto> variants,
        List<ProductTextileDto> textiles,
        List<ProductImageDto> images
) {
}