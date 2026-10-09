package com.example.clothesstore.size;

public record SizeDto(
        Integer id,
        String code,
        String label,
        SizeSystem system,
        Integer sortOrder
) {
}