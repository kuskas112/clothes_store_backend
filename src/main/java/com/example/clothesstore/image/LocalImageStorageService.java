package com.example.clothesstore.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalImageStorageService implements ImageStorageService {

    @Value("${app.storage.location}")
    private String rootLocation;

    public record ImageDimensions(Integer width, Integer height) {}

    @Override
    public ImageDto store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Rejected empty file");
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Rejected non-image file: name={}, contentType={}",
                    file.getOriginalFilename(), contentType);
            throw new IllegalArgumentException("Only images are allowed");
        }

        String subDir = LocalDate.now().toString();
        String extension = extractExtension(file.getOriginalFilename());
        String storageKey = subDir + "/" + UUID.randomUUID() + extension;

        Path root = Paths.get(rootLocation).normalize();
        Path target = root.resolve(storageKey).normalize();

        if (!target.startsWith(root)) {
            log.warn("Rejected storage key outside root: {}", storageKey);
            throw new IllegalArgumentException("Invalid storage key");
        }

        try {
            Files.createDirectories(target.getParent());

            byte[] bytes = file.getBytes();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String checksum = HexFormat.of().formatHex(digest.digest(bytes));

            Files.write(target, bytes);

            ImageDimensions dims = readDimensions(bytes);

            log.info("Stored image: key={}, size={} bytes, dimensions={}x{}, checksum={}",
                    storageKey, bytes.length,
                    dims != null ? dims.width() : null,
                    dims != null ? dims.height() : null,
                    checksum);

            return new ImageDto(
                    storageKey,
                    file.getOriginalFilename(),
                    contentType,
                    file.getSize(),
                    checksum,
                    dims != null ? dims.width() : null,
                    dims != null ? dims.height() : null
            );
        } catch (IOException | NoSuchAlgorithmException e) {
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
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(file.getBytes()));
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Failed to compute checksum for: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to compute checksum", e);
        }
    }

    private ImageDimensions readDimensions(byte[] bytes) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                log.debug("No ImageReader found for uploaded bytes");
                return null;
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(iis);
                return new ImageDimensions(reader.getWidth(0), reader.getHeight(0));
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            log.warn("Failed to read image dimensions", e);
            return null;
        }
    }

    private String extractExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot).toLowerCase() : "";
    }
}