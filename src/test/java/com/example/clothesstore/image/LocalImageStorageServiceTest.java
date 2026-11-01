package com.example.clothesstore.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalImageStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalImageStorageService service;

    @BeforeEach
    void setUp() {
        service = new LocalImageStorageService();
        ReflectionTestUtils.setField(service, "rootLocation", tempDir.toString());
    }

    @Test
    void storeWritesFileAndMetadata() throws IOException {
        Image image = service.store(pngFile(7, 5));

        assertThat(image.getStorageKey()).endsWith(".png");
        assertThat(image.getStorageKey()).matches("\\d{4}-\\d{2}-\\d{2}/[0-9a-f-]{36}\\.png");
        assertThat(image.getContentType()).isEqualTo("image/png");
        assertThat(image.getWidth()).isEqualTo(7);
        assertThat(image.getHeight()).isEqualTo(5);
        assertThat(image.getChecksum()).hasSize(64);
        assertThat(Files.exists(tempDir.resolve(image.getStorageKey()))).isTrue();
    }

    @Test
    void storeRejectsHtmlPretendingToBeImage() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png",
                "<html>not an image</html>".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a valid image");
    }

    @Test
    void storeRejectsSvg() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.svg", "image/svg+xml", "<svg/>".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only JPEG");
    }

    @Test
    void storeRejectsEmptyAndNullFiles() {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> service.store(empty))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
        assertThatThrownBy(() -> service.store(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void storeSupportsBmp() throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "bmp", out);
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.bmp", "image/bmp", out.toByteArray());

        // BMP есть в белом списке — проверяем, что он реально сохраняется.
        Image stored = service.store(file);
        assertThat(stored.getStorageKey()).endsWith(".bmp");
        assertThat(stored.getContentType()).isEqualTo("image/bmp");
    }

    @Test
    void loadRejectsPathTraversal() {
        assertThatThrownBy(() -> service.load("../outside.jpg"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to load file");
    }

    @Test
    void deleteRejectsPathTraversal() {
        assertThatThrownBy(() -> service.delete("../outside.jpg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid storage key");
    }

    @Test
    void deleteRemovesStoredFile() throws IOException {
        Image image = service.store(pngFile(2, 2));
        Path stored = tempDir.resolve(image.getStorageKey());
        assertThat(Files.exists(stored)).isTrue();

        service.delete(image.getStorageKey());

        assertThat(Files.exists(stored)).isFalse();
    }

    @Test
    void checksumIsStableAndDependsOnContent() throws IOException {
        MockMultipartFile first = pngFile(3, 3);
        MockMultipartFile same = pngFile(3, 3);
        MockMultipartFile different = pngFile(4, 4);

        String checksum = service.calculateChecksum(first);

        assertThat(checksum).hasSize(64).isEqualTo(service.calculateChecksum(same));
        assertThat(service.calculateChecksum(different)).isNotEqualTo(checksum);
    }

    private static MockMultipartFile pngFile(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.fillRect(0, 0, width, height);
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return new MockMultipartFile("file", "image.png", "image/png", out.toByteArray());
    }
}
