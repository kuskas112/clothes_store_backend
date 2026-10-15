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
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public List<CategoryDto> getAll() {
        List<Category> categories = categoryService.getAll();
        List<CategoryDto> result = new ArrayList<>();
        for (Category c : categories) {
            result.add(categoryMapper.toDto(c));
        }
        return result;
    }

    @GetMapping("/{id}")
    public CategoryDto getOne(@PathVariable Long id) {
        return categoryMapper.toDto(categoryService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@RequestBody @Valid CategoryCreateRequest request) {
        Category category = categoryMapper.toEntity(request);

        if (request.parentId() != null) {
            category.setParent(categoryService.getById(request.parentId()));
        }

        Category saved = categoryService.create(category);
        return categoryMapper.toDto(saved);
    }

    @PatchMapping("/{id}")
    public CategoryDto update(@PathVariable Long id,
                              @RequestBody @Valid CategoryPatchRequest request) {
        Category updated = categoryService.update(id, c -> {
            categoryMapper.updateEntity(request, c);
            if (request.parentId() != null) {
                c.setParent(categoryService.getById(request.parentId()));
            }
        });
        return categoryMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}