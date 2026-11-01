package com.example.clothesstore.product;

import com.example.clothesstore.color.Color;
import com.example.clothesstore.color.ColorRepository;
import com.example.clothesstore.exception.ProductNotFoundException;
import com.example.clothesstore.image.Image;
import com.example.clothesstore.image.ImageRepository;
import com.example.clothesstore.product.dto.*;
import com.example.clothesstore.product.dto.request.*;
import com.example.clothesstore.product.jpa.Product;
import com.example.clothesstore.product.jpa.ProductImage;
import com.example.clothesstore.product.jpa.ProductTextile;
import com.example.clothesstore.product.jpa.ProductVariant;
import com.example.clothesstore.size.Size;
import com.example.clothesstore.size.SizeRepository;
import com.example.clothesstore.textile.Textile;
import com.example.clothesstore.textile.TextileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final TextileRepository textileRepository;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;
    private final ImageRepository imageRepository;

    // ===== READ =====

    /**
     * Две выборки вместо N+1:
     * 1) страница товаров,
     * 2) все изображения для этих товаров через EntityGraph.
     */
    @Transactional(readOnly = true)
    public Page<ProductShortDto> getAll(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        List<Integer> ids = page.getContent().stream()
                .map(Product::getId)
                .toList();

        if (ids.isEmpty()) {
            return page.map(productMapper::toShortDto);
        }

        Map<Integer, Product> withImages = productRepository.findWithImagesByIdIn(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));

        return page.map(product -> productMapper.toShortDto(
                withImages.getOrDefault(product.getId(), product)));
    }

    @Transactional(readOnly = true)
    public ProductDto getOne(Integer id) {
        Product product = productRepository.findWithDetailsById(id).orElseThrow(() ->
                new ProductNotFoundException(
                        "Product with id `%s` not found".formatted(id)));
        return productMapper.toDto(product);
    }

    /**
     * Порядок ids сохраняется (findAllById его не гарантирует), изображения
     * подтягиваются одним запросом.
     */
    @Transactional(readOnly = true)
    public List<ProductShortDto> getMany(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        Map<Integer, Product> byId = productRepository.findWithImagesByIdIn(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity(), (a, b) -> a));

        return ids.stream()
                .distinct()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(productMapper::toShortDto)
                .toList();
    }

    // ===== CREATE =====

    @Transactional
    public ProductDto create(ProductCreateRequest request) {
        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    // ===== UPDATE =====

    @Transactional
    public ProductDto update(Integer id, ProductPatchRequest request) {
        Product product = productRepository.findWithDetailsById(id).orElseThrow(() ->
                new ProductNotFoundException("Product with id `%s` not found".formatted(id)));
        productMapper.updateEntity(request, product);
        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    // ===== DELETE =====

    @Transactional
    public void delete(Integer id) {
        Product product = productRepository.findById(id).orElseThrow(() ->
                new ProductNotFoundException("Product with id `%s` not found".formatted(id)));
        productRepository.delete(product);
    }

    @Transactional
    public void deleteMany(List<Integer> ids) {
        productRepository.deleteAllById(ids);
    }

    // ===== VARIANTS =====

    @Transactional
    public ProductDto addVariant(Integer productId, ProductVariantCreateRequest request) {
        Product product = productRepository.findWithDetailsById(productId).orElseThrow(() ->
                new ProductNotFoundException("Product with id `%s` not found".formatted(productId)));

        ProductVariant variant = productMapper.toEntity(request);

        if (request.sizeId() != null) {
            Size size = sizeRepository.findById(request.sizeId()).orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Size with id `%s` not found".formatted(request.sizeId())));
            variant.setSize(size);
        }

        if (request.colorId() != null) {
            Color color = colorRepository.findById(request.colorId()).orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Color with id `%s` not found".formatted(request.colorId())));
            variant.setColor(color);
        }

        product.addVariant(variant);

        // Дубликат (product, color, size) или sku ловит unique-констрейнт,
        // GlobalExceptionHandler вернёт 409.
        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto removeVariant(Integer productId, Integer variantId) {
        Product product = productRepository.findWithDetailsById(productId).orElseThrow(() ->
                new ProductNotFoundException("Product with id `%s` not found".formatted(productId)));

        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Variant with id `%s` is not linked to product `%s`"
                                .formatted(variantId, productId)));

        product.removeVariant(variant);

        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    // ===== TEXTILES =====

    @Transactional
    public ProductDto addTextile(Integer productId, ProductTextileRequest request) {
        Product product = productRepository.findWithDetailsById(productId).orElseThrow(() ->
                new ProductNotFoundException("Product with id `%s` not found".formatted(productId)));

        Textile textile = textileRepository.findById(request.textileId()).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Textile with id `%s` not found".formatted(request.textileId())));

        // Проверка: не привязана ли уже эта ткань к продукту
        boolean alreadyLinked = product.getTextiles().stream()
                .anyMatch(pt -> pt.getTextile().getId().equals(textile.getId()));
        if (alreadyLinked) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Textile with id `%s` is already linked to product `%s`"
                            .formatted(textile.getId(), productId));
        }

        product.addTextile(textile, request.percentage());

        ProductTextile link = product.getTextiles().stream()
                .filter(pt -> pt.getTextile().getId().equals(textile.getId()))
                .findFirst()
                .orElseThrow();

        // Основная ткань должна быть одна: снимаем флаг с остальных.
        if (Boolean.TRUE.equals(request.primaryFabric())) {
            product.getTextiles().forEach(pt -> pt.setPrimaryFabric(false));
            link.setPrimaryFabric(true);
        } else if (request.primaryFabric() != null) {
            link.setPrimaryFabric(false);
        }

        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto removeTextile(Integer productId, Integer textileId) {
        Product product = productRepository.findWithDetailsById(productId).orElseThrow(() ->
                new ProductNotFoundException("Product with id `%s` not found".formatted(productId)));

        boolean removed = product.getTextiles().removeIf(
                pt -> pt.getTextile().getId().equals(textileId));

        if (!removed) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Textile with id `%s` is not linked to product `%s`"
                            .formatted(textileId, productId));
        }

        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    // ===== IMAGES =====

    @Transactional
    public ProductDto addImage(Integer productId, ProductImageRequest request) {
        Product product = productRepository.findWithDetailsById(productId).orElseThrow(() ->
                new ProductNotFoundException("Product with id `%s` not found".formatted(productId)));

        Image image = imageRepository.findById(request.imageId()).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Image with id `%s` not found".formatted(request.imageId())));

        if (product.getImages().stream()
                .anyMatch(pi -> pi.getImage().getId().equals(image.getId()))) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Image with id `%s` is already attached to product `%s`"
                            .formatted(image.getId(), productId));
        }

        ProductImage pi = new ProductImage();
        pi.setImage(image);
        pi.setAltText(request.altText());
        pi.setSortOrder(request.sortOrder() != null ? request.sortOrder() : nextSortOrder(product));
        pi.setIsMain(Boolean.TRUE.equals(request.isMain()));

        if (pi.getIsMain()) {
            product.getImages().forEach(existing -> existing.setIsMain(false));
        }

        product.addImage(pi);
        Product saved = productRepository.save(product);

        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto removeImage(Integer productId, Long productImageId) {
        Product product = productRepository.findWithDetailsById(productId).orElseThrow(() ->
                new ProductNotFoundException("Product with id `%s` not found".formatted(productId)));

        ProductImage pi = product.getImages().stream()
                .filter(x -> x.getId().equals(productImageId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product image with id `%s` not found".formatted(productImageId)));

        boolean wasMain = Boolean.TRUE.equals(pi.getIsMain());
        product.removeImage(pi);

        // если удалили главную — назначить первую оставшуюся
        if (wasMain && !product.getImages().isEmpty()) {
            product.getImages().iterator().next().setIsMain(true);
        }

        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto updateImage(Integer productId, Long productImageId, ProductImagePatchRequest request) {
        Product product = productRepository.findWithDetailsById(productId).orElseThrow(() ->
                new ProductNotFoundException("Product with id `%s` not found".formatted(productId)));

        ProductImage pi = product.getImages().stream()
                .filter(x -> x.getId().equals(productImageId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product image with id `%s` not found".formatted(productImageId)));

        if (request.altText() != null) {
            pi.setAltText(request.altText());
        }
        if (request.sortOrder() != null) {
            pi.setSortOrder(request.sortOrder());
        }
        if (request.isMain() != null) {
            if (request.isMain()) {
                product.getImages().forEach(existing -> existing.setIsMain(false));
            }
            pi.setIsMain(request.isMain());
        }

        Product saved = productRepository.save(product);

        return productMapper.toDto(saved);
    }

    private static int nextSortOrder(Product product) {
        return product.getImages().stream()
                .map(ProductImage::getSortOrder)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(-1) + 1;
    }
}
