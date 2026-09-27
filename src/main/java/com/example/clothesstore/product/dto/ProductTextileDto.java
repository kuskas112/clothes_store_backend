package com.example.clothesstore.product.dto;

public record ProductTextileDto(
        Integer id,
        Integer textileId,
        String textileName,
        Integer percentage,
        Boolean primaryFabric
) {
}