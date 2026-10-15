package com.example.clothesstore.category;

import com.example.clothesstore.exception.CategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> getAllActive() {
        return categoryRepository.findAllByActiveTrueOrderBySortOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new CategoryNotFoundException(slug));
    }

    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    @Transactional
    public Category create(Category category) {
        if (categoryRepository.existsBySlug(category.getSlug())) {
            throw new IllegalArgumentException("Category with slug already exists: " + category.getSlug());
        }
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category with name already exists: " + category.getName());
        }

        if (category.getSortOrder() == null) category.setSortOrder(0);
        if (category.getActive() == null) category.setActive(true);

        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, Consumer<Category> updater) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        updater.accept(category);

        return category;
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        categoryRepository.delete(category);
    }
}