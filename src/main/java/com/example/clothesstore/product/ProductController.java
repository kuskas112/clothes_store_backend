package com.example.clothesstore.product;

import com.example.clothesstore.category.CategoryMapper;
import com.example.clothesstore.category.dto.CategoryRefDto;
import com.example.clothesstore.product.dto.ProductDto;
import com.example.clothesstore.product.dto.ProductShortDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private final ProductCategoryService productCategoryService;

    private final CategoryMapper categoryMapper;

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

    @GetMapping("/{productId}/categories")
    public List<CategoryRefDto> getCategoriesByProductId(@PathVariable Integer productId) {
        return productCategoryService.getCategoriesForProduct(productId).stream().map(categoryMapper::toRefDto).toList();
    }
}