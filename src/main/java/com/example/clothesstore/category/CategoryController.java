package com.example.clothesstore.category;

import com.example.clothesstore.category.dto.request.CategoryCreateRequest;
import com.example.clothesstore.category.dto.request.CategoryPatchRequest;
import com.example.clothesstore.category.dto.CategoryDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public List<CategoryDto> getAll() {
        List<Category> categories = categoryService.getAllActive();
        List<CategoryDto> result = new ArrayList<>();
        for (Category c : categories) {
            result.add(categoryMapper.toDto(c));
        }
        return result;
    }

    @GetMapping("/{slug}")
    public CategoryDto getBySlug(@PathVariable String slug) {
        Category category = categoryService.getBySlug(slug);
        return categoryMapper.toDto(category);
    }
}