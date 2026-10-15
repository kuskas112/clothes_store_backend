package com.example.clothesstore.product.dto.request;

import com.example.clothesstore.product.jpa.Gender;
import com.example.clothesstore.product.jpa.Season;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductPatchRequest(
        String name,
        Gender gender,
        Season season,
        BigDecimal basePrice,
        @Size(max = 10000)
        String description) {
}