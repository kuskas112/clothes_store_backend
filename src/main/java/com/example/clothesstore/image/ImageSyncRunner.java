package com.example.clothesstore.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageSyncRunner implements ApplicationRunner {
    // Синхронизирует изображения в uploads c БД для тестов

    private final ImageRepository imageRepository;

    @Value("${app.storage.location}")
    private String rootLocation;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Path root = Paths.get(rootLocation).normalize();
        if (!Files.exists(root)) {
            log.info("Uploads directory does not exist: {}", root);
            return;
        }

        int created = 0;

        try (Stream<Path> files = Files.walk(root)) {
            Iterator<Path> it = files.filter(Files::isRegularFile).iterator();
            while (it.hasNext()) {
                Path file = it.next();
                String storageKey = root.relativize(file).toString().replace('\\', '/');

                if (imageRepository.existsByStorageKey(storageKey)) {
                    continue;
                }

                try {
                    byte[] bytes = Files.readAllBytes(file);
                    String checksum = sha256(bytes);

                    if (imageRepository.findByChecksum(checksum).isPresent()) {
                        log.debug("Skipping {} — checksum already registered", storageKey);
                        continue;
                    }

                    Image image = new Image();
                    image.setStorageKey(storageKey);
                    image.setOriginalFilename(file.getFileName().toString());
                    image.setContentType(Files.probeContentType(file));
                    image.setSizeBytes(Files.size(file));
                    image.setChecksum(checksum);

                    ImageDimensions dims = readDimensions(bytes);
                    if (dims != null) {
                        image.setWidth(dims.width());
                        image.setHeight(dims.height());
                    }

                    imageRepository.save(image);
                    created++;
                } catch (Exception e) {
                    log.warn("Failed to sync file: {}", storageKey, e);
                }
            }
        } catch (IOException e) {
            log.error("Failed to scan uploads directory", e);
        }

        log.info("Image sync finished: {} new records", created);
    }

    private String sha256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(bytes));
    }

    private ImageDimensions readDimensions(byte[] bytes) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) return null;
            ImageReader reader = readers.next();
            try {
                reader.setInput(iis);
                return new ImageDimensions(reader.getWidth(0), reader.getHeight(0));
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            return null;
        }
    }

    public record ImageDimensions(Integer width, Integer height) {}
}