package com.example.clothesstore.product.dto.request;

import com.example.clothesstore.product.jpa.Gender;
import com.example.clothesstore.product.jpa.Season;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductPatchRequest(
        @Size(max = 255)
        String name,

        Gender gender,

        Season season,

        @PositiveOrZero
        BigDecimal basePrice,

        @PositiveOrZero
        BigDecimal discountedPrice,

        @Size(max = 10000)
        String description) {
}
