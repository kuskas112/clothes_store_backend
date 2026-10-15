package com.example.clothesstore.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String slug) {
        super("Category not found: " + slug);
    }
    public CategoryNotFoundException(Long id) {
        super("Category not found: " + id);
    }
}