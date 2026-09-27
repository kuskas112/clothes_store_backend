package com.example.clothesstore.product.dto;

import com.example.clothesstore.product.Gender;
import com.example.clothesstore.product.Season;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank @Size(max = 255)
        String name,

        Gender gender,

        Season season,

        @PositiveOrZero
        BigDecimal basePrice
) {
}