package com.example.clothesstore.product.size;

import com.example.clothesstore.product.size.SizeSystem;

public record SizeDto(
        Integer id,
        String code,
        String label,
        SizeSystem system,
        Integer sortOrder
) {
}