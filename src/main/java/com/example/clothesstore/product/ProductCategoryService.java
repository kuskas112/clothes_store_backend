package com.example.clothesstore.product;

import com.example.clothesstore.category.Category;
import com.example.clothesstore.category.CategoryService;
import com.example.clothesstore.exception.ProductNotFoundException;
import com.example.clothesstore.product.jpa.Product;
import com.example.clothesstore.product.jpa.ProductCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Transactional
    public void addCategory(Integer productId, Long categoryId, Integer sortOrder, Boolean isPrimary) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with id `%s` not found".formatted(productId)));

        if (productCategoryRepository.existsByProductIdAndCategoryId(productId, categoryId)) {
            throw new IllegalArgumentException(
                    "Category %d is already linked to product %d".formatted(categoryId, productId));
        }

        Category category = categoryService.getById(categoryId);

        ProductCategory link = new ProductCategory();
        link.setProduct(product);
        link.setCategory(category);
        link.setSortOrder(sortOrder != null ? sortOrder : 0);
        link.setIsPrimary(Boolean.TRUE.equals(isPrimary));

        if (link.getIsPrimary()) {
            clearPrimaryForProduct(productId);
        }

        productCategoryRepository.save(link);
    }

    @Transactional
    public void removeCategory(Integer productId, Long productCategoryId) {
        ProductCategory link = productCategoryRepository.findById(productCategoryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ProductCategory %d not found".formatted(productCategoryId)));

        if (!link.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException(
                    "ProductCategory %d does not belong to product %d".formatted(productCategoryId, productId));
        }

        productCategoryRepository.delete(link);
    }

    @Transactional
    public void updateCategory(Integer productId, Long productCategoryId, Integer sortOrder, Boolean isPrimary) {
        ProductCategory link = productCategoryRepository.findById(productCategoryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ProductCategory %d not found".formatted(productCategoryId)));

        if (!link.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException(
                    "ProductCategory %d does not belong to product %d".formatted(productCategoryId, productId));
        }

        if (sortOrder != null) {
            link.setSortOrder(sortOrder);
        }

        if (isPrimary != null) {
            if (isPrimary) {
                clearPrimaryForProduct(productId);
            }
            link.setIsPrimary(isPrimary);
        }
    }

    @Transactional(readOnly = true)
    public List<Category> getCategoriesForProduct(Integer productId) {
        List<ProductCategory> links = productCategoryRepository.findByProductIdOrderBySortOrderAscIdAsc(productId);
        List<Category> result = new ArrayList<>();
        for (ProductCategory link : links) {
            result.add(link.getCategory());
        }
        return result;
    }

    private void clearPrimaryForProduct(Integer productId) {
        List<ProductCategory> links = productCategoryRepository.findByProductId(productId);
        for (ProductCategory link : links) {
            if (Boolean.TRUE.equals(link.getIsPrimary())) {
                link.setIsPrimary(false);
            }
        }
    }
}