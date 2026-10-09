package com.example.clothesstore.image;

public record ImageDto(
        String storageKey,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String checksum,
        Integer width,
        Integer height
) {}