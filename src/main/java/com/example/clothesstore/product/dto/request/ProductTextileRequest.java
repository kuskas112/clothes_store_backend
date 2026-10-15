package com.example.clothesstore.product.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductTextileRequest(
        @NotNull
        Integer textileId,

        @NotNull @Min(0) @Max(100)
        Integer percentage,

        Boolean primaryFabric
) {
}