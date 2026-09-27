package com.example.clothesstore.product.dto;

import com.example.clothesstore.product.color.ColorDto;
import com.example.clothesstore.product.size.SizeDto;

import java.math.BigDecimal;

public record ProductVariantDto(
        Integer id,
        String sku,
        ColorDto color,
        SizeDto size,
        Integer stockQuantity,
        BigDecimal price
) {
}