package com.example.clothesstore.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalImageStorageService implements ImageStorageService {

    /** Форматы, которые реально умеет читать ImageIO в этом образе. */
    private static final Map<String, String> FORMAT_TO_EXTENSION = Map.of(
            "jpeg", ".jpg",
            "jpg", ".jpg",
            "png", ".png",
            "gif", ".gif",
            "bmp", ".bmp"
    );

    private static final Set<String> ALLOWED_DECLARED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp"
    );

    /** Защита от decompression bomb: 40 MP и не более 20000px по стороне. */
    private static final long MAX_PIXELS = 40_000_000L;
    private static final int MAX_SIDE = 20_000;

    @Value("${app.storage.location}")
    private String rootLocation;

    public record ImageDimensions(Integer width, Integer height) {
    }

    private record ProbedImage(String format, int width, int height) {
    }

    @Override
    public Image store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Rejected empty file");
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_DECLARED_TYPES.contains(contentType.toLowerCase())) {
            log.warn("Rejected non-image file: name={}, contentType={}",
                    file.getOriginalFilename(), contentType);
            throw new IllegalArgumentException("Only JPEG, PNG, GIF and BMP images are allowed");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        // Проверяем содержимое, а не только заголовок Content-Type:
        // так отсекаются SVG/HTML/произвольные байты под видом картинки.
        ProbedImage probed = probe(bytes);
        if (probed == null) {
            log.warn("Rejected file that is not a readable image: {}", file.getOriginalFilename());
            throw new IllegalArgumentException("File is not a valid image");
        }
        if (probed.width() <= 0 || probed.height() <= 0
                || probed.width() > MAX_SIDE || probed.height() > MAX_SIDE
                || (long) probed.width() * probed.height() > MAX_PIXELS) {
            log.warn("Rejected image with invalid dimensions {}x{}", probed.width(), probed.height());
            throw new IllegalArgumentException("Image dimensions are too large");
        }

        String extension = FORMAT_TO_EXTENSION.get(probed.format());
        if (extension == null) {
            log.warn("Rejected image with unsupported format: {}", probed.format());
            throw new IllegalArgumentException("Unsupported image format: " + probed.format());
        }

        String subDir = LocalDate.now().toString();
        String storageKey = subDir + "/" + UUID.randomUUID() + extension;

        Path root = Paths.get(rootLocation).normalize();
        Path target = root.resolve(storageKey).normalize();

        if (!target.startsWith(root)) {
            log.warn("Rejected storage key outside root: {}", storageKey);
            throw new IllegalArgumentException("Invalid storage key");
        }

        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);

            String checksum = sha256(bytes);

            log.info("Stored image: key={}, size={} bytes, dimensions={}x{}, checksum={}",
                    storageKey, bytes.length, probed.width(), probed.height(), checksum);

            var image = new Image();
            image.setStorageKey(storageKey);
            image.setOriginalFilename(safeOriginalFilename(file.getOriginalFilename(), extension));
            image.setContentType("image/" + ("jpg".equals(probed.format()) ? "jpeg" : probed.format()));
            image.setSizeBytes((long) bytes.length);
            image.setChecksum(checksum);
            image.setWidth(probed.width());
            image.setHeight(probed.height());

            return image;
        } catch (IOException e) {
            log.error("Failed to store image: name={}, key={}", file.getOriginalFilename(), storageKey, e);
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public Resource load(String storageKey) {
        try {
            Path root = Paths.get(rootLocation).normalize();
            Path path = root.resolve(storageKey).normalize();
            if (!path.startsWith(root)) {
                log.warn("Rejected load outside root: {}", storageKey);
                throw new IllegalArgumentException("Invalid storage key");
            }
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                log.warn("File not found or unreadable: {}", storageKey);
                throw new RuntimeException("File not found: " + storageKey);
            }
            log.debug("Loaded image: {}", storageKey);
            return resource;
        } catch (Exception e) {
            log.error("Failed to load image: {}", storageKey, e);
            throw new RuntimeException("Failed to load file", e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path root = Paths.get(rootLocation).normalize();
            Path path = root.resolve(storageKey).normalize();
            if (!path.startsWith(root)) {
                log.warn("Rejected delete outside root: {}", storageKey);
                throw new IllegalArgumentException("Invalid storage key");
            }
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("Deleted image: {}", storageKey);
            } else {
                log.warn("Image not found for delete: {}", storageKey);
            }
        } catch (IOException e) {
            log.error("Failed to delete image: {}", storageKey, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    @Override
    public String calculateChecksum(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        try {
            return sha256(file.getBytes());
        } catch (IOException e) {
            log.error("Failed to compute checksum for: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to compute checksum", e);
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private ProbedImage probe(byte[] bytes) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(iis, true, true);
                return new ProbedImage(
                        reader.getFormatName().toLowerCase(),
                        reader.getWidth(0),
                        reader.getHeight(0)
                );
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            log.warn("Failed to probe image", e);
            return null;
        }
    }

    /**
     * Имя файла не участвует в пути (путь = UUID), но всё равно чистим
     * управляющие символы и ограничиваем длину — оно попадает в ответы API.
     */
    private static String safeOriginalFilename(String originalFilename, String extension) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "image" + extension;
        }
        String cleaned = originalFilename.replaceAll("[\\p{Cntrl}\\\\/]", "_");
        return cleaned.length() > 255 ? cleaned.substring(0, 255) : cleaned;
    }
}
