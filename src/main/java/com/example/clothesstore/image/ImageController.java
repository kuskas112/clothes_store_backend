package com.example.clothesstore.image;

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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> upload(@RequestParam("file") MultipartFile file) {
        ImageService.UploadResult result = imageService.upload(file);

        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        if (result.created()) {
            log.info("Registered image: id={}, key={}",
                    result.image().getId(), result.image().getStorageKey());
        }
        return ResponseEntity.status(status).body(imageMapper.toResponse(result.image()));
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
        List<ImageResponse> result = imageService.uploadMany(files).stream()
                .map(upload -> imageMapper.toResponse(upload.image()))
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
