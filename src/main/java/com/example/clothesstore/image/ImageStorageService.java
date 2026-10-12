package com.example.clothesstore.image;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    Image store(MultipartFile file);
    Resource load(String storageKey);
    void delete(String storageKey);
    String calculateChecksum(MultipartFile file);
}