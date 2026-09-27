package com.example.clothesstore.product;

import com.example.clothesstore.product.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

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
}