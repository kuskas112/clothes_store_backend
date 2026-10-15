package com.example.clothesstore.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductVariantCreateRequest(
        @NotBlank @Size(max = 100)
        String sku,

        Integer colorId,

        Integer sizeId,

        @NotNull @PositiveOrZero
        Integer stockQuantity,

        @PositiveOrZero
        BigDecimal price
) {
}