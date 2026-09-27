package com.example.clothesstore.product.dto;

import com.example.clothesstore.product.Gender;
import com.example.clothesstore.product.Season;

import java.math.BigDecimal;

public record ProductPatchRequest(
        String name,
        Gender gender,
        Season season,
        BigDecimal basePrice
) {
}