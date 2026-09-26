package com.example.clothesstore.textile;

import java.math.BigDecimal;

public record TextileDto(
        Integer id,
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