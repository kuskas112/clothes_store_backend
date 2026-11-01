package com.example.clothesstore.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ImageMapperTest {

    private ImageMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ImageMapper();
        ReflectionTestUtils.setField(mapper, "baseUrl", "http://localhost:8080/files");
    }

    @Test
    void buildsAbsoluteUrlFromStorageKey() {
        ImageResponse response = mapper.toResponse(image("2026-11-01/abc.jpg"));

        assertThat(response.url()).isEqualTo("http://localhost:8080/files/2026-11-01/abc.jpg");
    }

    @Test
    void trimsTrailingSlashInBaseUrl() {
        ReflectionTestUtils.setField(mapper, "baseUrl", "/files/");

        assertThat(mapper.toResponse(image("a.jpg")).url()).isEqualTo("/files/a.jpg");
    }

    @Test
    void supportsRelativeBaseUrl() {
        ReflectionTestUtils.setField(mapper, "baseUrl", "/files");

        assertThat(mapper.toResponse(image("a.jpg")).url()).isEqualTo("/files/a.jpg");
    }

    @Test
    void mapsImageMetadata() {
        Image image = image("a.jpg");
        image.setId(42L);
        image.setAltText("пальто");
        image.setWidth(100);
        image.setHeight(200);

        ImageResponse response = mapper.toResponse(image);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.altText()).isEqualTo("пальто");
        assertThat(response.width()).isEqualTo(100);
        assertThat(response.height()).isEqualTo(200);
    }

    @Test
    void mapsEntityToDto() {
        Image image = image("a.jpg");

        ImageDto dto = mapper.toDto(image);

        assertThat(dto.storageKey()).isEqualTo("a.jpg");
        assertThat(dto.contentType()).isEqualTo("image/jpeg");
        assertThat(dto.sizeBytes()).isEqualTo(10L);
    }

    private static Image image(String storageKey) {
        Image image = new Image();
        image.setStorageKey(storageKey);
        image.setContentType("image/jpeg");
        image.setSizeBytes(10L);
        return image;
    }
}
