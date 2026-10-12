package com.example.clothesstore.image;

import jakarta.transaction.Transactional;
import jdk.jfr.TransitionTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageStorageService storageService;
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> upload(@RequestParam("file") MultipartFile file) {
        String checksum = storageService.calculateChecksum(file);

        Optional<Image> existing = imageRepository.findByChecksum(checksum);
        if (existing.isPresent()) {
            return ResponseEntity.ok(imageMapper.toResponse(existing.get()));
        }

        Image stored = storageService.store(file);
        Image saved = imageRepository.save(stored);
        log.info("Registered image: id={}, key={}", saved.getId(), saved.getStorageKey());
        return ResponseEntity.status(HttpStatus.CREATED).body(imageMapper.toResponse(saved));
    }

    @GetMapping
    public PagedModel<ImageResponse> list(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ImageResponse> page = imageRepository.findAll(pageable)
                .map(imageMapper::toResponse);
        return new PagedModel<>(page);
    }

    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ImageResponse>> uploadMany(
            @RequestParam("files") List<MultipartFile> files) {
        List<ImageResponse> result = new ArrayList<>();
        for (MultipartFile file : files) {
            String checksum = storageService.calculateChecksum(file);
            Image image = imageRepository.findByChecksum(checksum)
                    .orElseGet(() -> {
                        Image stored = storageService.store(file);
                        return imageRepository.save(stored);
                    });
            result.add(imageMapper.toResponse(image));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}