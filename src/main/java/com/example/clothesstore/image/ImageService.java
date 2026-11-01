package com.example.clothesstore.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Логика загрузки изображений с дедупликацией по SHA-256.
 * Вынесена из контроллера, чтобы контроллер не зависел от репозитория.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageStorageService storageService;
    private final ImageRepository imageRepository;

    @Value("${app.upload.max-batch-size:20}")
    private int maxBatchSize;

    public record UploadResult(Image image, boolean created) {
    }

    public UploadResult upload(MultipartFile file) {
        String checksum = storageService.calculateChecksum(file);

        return imageRepository.findByChecksum(checksum)
                .map(image -> new UploadResult(image, false))
                .orElseGet(() -> new UploadResult(storeNew(file, checksum), true));
    }

    /**
     * Батч-загрузка: один запрос к БД на все checksum-ы вместо запроса на файл.
     */
    public List<UploadResult> uploadMany(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided");
        }
        if (files.size() > maxBatchSize) {
            throw new IllegalArgumentException(
                    "Too many files in one request: %d (max %d)".formatted(files.size(), maxBatchSize));
        }

        // checksum -> файл (одинаковые файлы внутри батча загружаем один раз)
        Map<String, MultipartFile> byChecksum = new LinkedHashMap<>();
        for (MultipartFile file : files) {
            byChecksum.putIfAbsent(storageService.calculateChecksum(file), file);
        }

        Map<String, Image> existing = imageRepository.findByChecksumIn(byChecksum.keySet()).stream()
                .collect(Collectors.toMap(Image::getChecksum, Function.identity(), (a, b) -> a));

        List<UploadResult> result = new ArrayList<>(byChecksum.size());
        for (Map.Entry<String, MultipartFile> entry : byChecksum.entrySet()) {
            Image image = existing.get(entry.getKey());
            if (image != null) {
                result.add(new UploadResult(image, false));
            } else {
                result.add(new UploadResult(storeNew(entry.getValue(), entry.getKey()), true));
            }
        }
        return result;
    }

    private Image storeNew(MultipartFile file, String checksum) {
        Image stored = storageService.store(file);
        stored.setChecksum(checksum);

        try {
            return imageRepository.saveAndFlush(stored);
        } catch (DataIntegrityViolationException ex) {
            // Конкурентный запрос успел сохранить тот же checksum.
            // Файл на диске больше не нужен — удаляем, возвращаем существующую запись.
            return imageRepository.findByChecksum(checksum)
                    .map(existing -> {
                        safeDelete(stored.getStorageKey());
                        return existing;
                    })
                    .orElseThrow(() -> ex);
        }
    }

    private void safeDelete(String storageKey) {
        try {
            storageService.delete(storageKey);
        } catch (RuntimeException e) {
            log.warn("Failed to clean up orphan image {}: {}", storageKey, e.getMessage());
        }
    }
}
