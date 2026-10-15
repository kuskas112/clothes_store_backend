package com.example.clothesstore.product;

import com.example.clothesstore.category.CategoryMapper;
import com.example.clothesstore.category.dto.CategoryRefDto;
import com.example.clothesstore.product.dto.*;
import com.example.clothesstore.product.dto.request.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final ProductCategoryService productCategoryService;
    private  final CategoryMapper categoryMapper;


    // ===== CRUD =====

    @GetMapping
    public PagedModel<ProductShortDto> getAll(Pageable pageable) {
        Page<ProductShortDto> page = productService.getAll(pageable);
        return new PagedModel<>(page);
    }

    @GetMapping("/{id}")
    public ProductDto getOne(@PathVariable Integer id) {
        return productService.getOne(id);
    }

    @GetMapping("/by-ids")
    public List<ProductShortDto> getMany(@RequestParam List<Integer> ids) {
        return productService.getMany(ids);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto create(@RequestBody @Valid ProductCreateRequest request) {
        return productService.create(request);
    }

    @PatchMapping("/{id}")
    public ProductDto update(@PathVariable Integer id,
                             @RequestBody ProductPatchRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        productService.delete(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMany(@RequestParam List<Integer> ids) {
        productService.deleteMany(ids);
    }

    // ===== VARIANTS =====

    @PostMapping("/{productId}/variants")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto addVariant(@PathVariable Integer productId,
                                 @RequestBody @Valid ProductVariantCreateRequest request) {
        return productService.addVariant(productId, request);
    }

    @DeleteMapping("/{productId}/variants/{variantId}")
    public ProductDto removeVariant(@PathVariable Integer productId,
                                    @PathVariable Integer variantId) {
        return productService.removeVariant(productId, variantId);
    }

    // ===== IMAGES =====

    @PostMapping("/{productId}/images")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto addImage(@PathVariable Integer productId,
                               @RequestBody @Valid ProductImageRequest request) {
        return productService.addImage(productId, request);
    }

    @DeleteMapping("/{productId}/images/{productImageId}")
    public ProductDto removeImage(@PathVariable Integer productId,
                                  @PathVariable Long productImageId) {
        return productService.removeImage(productId, productImageId);
    }

    @PatchMapping("/{productId}/images/{productImageId}")
    public ProductDto updateImage(@PathVariable Integer productId,
                                  @PathVariable Long productImageId,
                                  @RequestBody @Valid ProductImagePatchRequest request) {
        return productService.updateImage(productId, productImageId, request);
    }

    // ===== CATEGORIES =====
    @GetMapping("/{productId}/categories")
    public List<CategoryRefDto> getCategoriesByProductId(@PathVariable Integer productId) {
        return productCategoryService.getCategoriesForProduct(productId).stream().map(categoryMapper::toRefDto).toList();
    }


    @PostMapping("/{productId}/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public List<CategoryRefDto> addCategory(@PathVariable Integer productId,
                                            @RequestBody @Valid ProductCategoryRequest request) {
        productCategoryService.addCategory(productId, request.categoryId(), request.sortOrder(), request.isPrimary());
        return productCategoryService.getCategoriesForProduct(productId).stream().map(categoryMapper::toRefDto).toList();
    }

    @PatchMapping("/{productId}/categories/{productCategoryId}")
    public List<CategoryRefDto> updateCategory(@PathVariable Integer productId,
                                               @PathVariable Long productCategoryId,
                                               @RequestBody @Valid ProductCategoryPatchRequest request) {
        productCategoryService.updateCategory(productId, productCategoryId, request.sortOrder(), request.isPrimary());
        return productCategoryService.getCategoriesForProduct(productId).stream().map(categoryMapper::toRefDto).toList();
    }

    @DeleteMapping("/{productId}/categories/{productCategoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCategory(@PathVariable Integer productId,
                               @PathVariable Long productCategoryId) {
        productCategoryService.removeCategory(productId, productCategoryId);
    }
}