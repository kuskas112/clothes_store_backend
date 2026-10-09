package com.example.clothesstore.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper {

    @Value("${app.storage.base-url}")
    private String baseUrl;

    public ImageDto toDto(Image image) {
        return new ImageDto(
                image.getStorageKey(),
                image.getOriginalFilename(),
                image.getContentType(),
                image.getSizeBytes(),
                image.getChecksum(),
                image.getWidth(),
                image.getHeight()
        );
    }

    public Image toEntity(ImageDto dto) {
        Image image = new Image();
        image.setStorageKey(dto.storageKey());
        image.setOriginalFilename(dto.originalFilename());
        image.setContentType(dto.contentType());
        image.setSizeBytes(dto.sizeBytes());
        image.setChecksum(dto.checksum());
        image.setWidth(dto.width());
        image.setHeight(dto.height());
        return image;
    }

    public ImageResponse toResponse(Image image) {
        return new ImageResponse(
                image.getId(),
                baseUrl + "/" + image.getStorageKey(),
                image.getAltText(),
                image.getWidth(),
                image.getHeight()
        );
    }
}