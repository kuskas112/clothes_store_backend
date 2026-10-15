package com.example.clothesstore.product.dto;

import com.example.clothesstore.product.jpa.Gender;
import com.example.clothesstore.product.jpa.Season;

import java.math.BigDecimal;

public record ProductShortDto(
        Integer id,
        String name,
        Gender gender,
        Season season,
        BigDecimal basePrice,
        String previewImageUrl
) {
}