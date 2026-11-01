package com.example.clothesstore.image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @BeforeEach
    void cleanUploads() throws IOException {
        Path root = Path.of("target/test-uploads");
        if (!Files.exists(root)) {
            return;
        }
        try (var walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder())
                    .filter(path -> !path.equals(root))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // тестовый каталог, ошибка уборки не должна ломать тест
                        }
                    });
        }
    }

    @Test
    void uploadStoresNewImageAndDeduplicates() throws IOException {
        byte[] png = pngBytes(Color.RED);

        ImageService.UploadResult first = imageService.upload(pngFile(png));
        ImageService.UploadResult second = imageService.upload(pngFile(png));

        assertThat(first.created()).isTrue();
        assertThat(second.created()).isFalse();
        assertThat(second.image().getId()).isEqualTo(first.image().getId());
        assertThat(imageRepository.count()).isEqualTo(1);
        assertThat(Files.exists(Path.of("target/test-uploads").resolve(first.image().getStorageKey())))
                .isTrue();
    }

    @Test
    void uploadManyDeduplicatesIdenticalFilesInBatch() throws IOException {
        byte[] png = pngBytes(Color.GREEN);

        List<ImageService.UploadResult> results =
                imageService.uploadMany(List.of(pngFile(png), pngFile(png)));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().created()).isTrue();
        assertThat(imageRepository.count()).isEqualTo(1);
    }

    @Test
    void uploadManyRejectsBatchOverConfiguredLimit() throws IOException {
        byte[] png = pngBytes(Color.BLUE);
        List<MultipartFile> files = IntStream.range(0, 21)
                .mapToObj(i -> (MultipartFile) pngFile(png))
                .toList();

        assertThatThrownBy(() -> imageService.uploadMany(files))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Too many files");
    }

    @Test
    void uploadRejectsHtmlDisguisedAsImage() {
        MultipartFile fake = new MockMultipartFile(
                "file", "avatar.png", "image/png", "<html><script>alert(1)</script></html>".getBytes());

        assertThatThrownBy(() -> imageService.upload(fake))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a valid image");
    }

    @Test
    void uploadRejectsSvg() {
        MultipartFile svg = new MockMultipartFile(
                "file", "logo.svg", "image/svg+xml", "<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes());

        assertThatThrownBy(() -> imageService.upload(svg))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JPEG");
    }

    @Test
    void uploadRejectsEmptyFile() {
        MultipartFile empty = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> imageService.upload(empty))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    private static MultipartFile pngFile(byte[] content) {
        return new MockMultipartFile("file", "test.png", "image/png", content);
    }

    private static byte[] pngBytes(Color color) throws IOException {
        BufferedImage image = new BufferedImage(8, 6, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(color);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        } finally {
            graphics.dispose();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
