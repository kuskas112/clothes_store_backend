package com.example.clothesstore.textile;

import java.math.BigDecimal;

public record TextilePatchDto(
        String name,
        String description,
        String composition,
        Integer densityGsm,
        WeaveType weaveType,
        Season season,
        Stretch stretch,
        String careInstructions,
        BigDecimal priceModifier,
        Boolean active
) {
}