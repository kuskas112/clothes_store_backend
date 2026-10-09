package com.example.clothesstore.image;

public record ImageResponse(
        Long id,
        String url,
        String altText,
        Integer width,
        Integer height
) {}