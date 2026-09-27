package com.example.clothesstore.product;

import com.example.clothesstore.product.color.Color;
import com.example.clothesstore.product.color.ColorRepository;
import com.example.clothesstore.product.dto.*;
import com.example.clothesstore.product.size.Size;
import com.example.clothesstore.product.size.SizeRepository;
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

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final TextileRepository textileRepository;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;

    // ===== READ =====

    @Transactional(readOnly = true)
    public Page<ProductShortDto> getAll(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toShortDto);
    }

    @Transactional(readOnly = true)
    public ProductDto getOne(Integer id) {
        Product product = productRepository.findWithDetailsById(id).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with id `%s` not found".formatted(id)));
        return productMapper.toDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductShortDto> getMany(List<Integer> ids) {
        return productRepository.findAllById(ids).stream()
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
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with id `%s` not found".formatted(id)));
        productMapper.updateEntity(request, product);
        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    // ===== DELETE =====

    @Transactional
    public void delete(Integer id) {
        Product product = productRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with id `%s` not found".formatted(id)));
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
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with id `%s` not found".formatted(productId)));

        ProductVariant variant = productMapper.toEntity(request);

        if(request.sizeId() != null){
            Size size = sizeRepository.findById(request.sizeId()).orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Size with id `%s` not found".formatted(request.sizeId()))
            );
            variant.setSize(size);
        }

        if (request.colorId() != null) {
            Color color = colorRepository.findById(request.colorId()).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Color with id `%s` not found".formatted(request.colorId())
                )
            );
            variant.setColor(color);
        }

        product.addVariant(variant);

        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto removeVariant(Integer productId, Integer variantId) {
        Product product = productRepository.findWithDetailsById(productId).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with id `%s` not found".formatted(productId)));

        product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .ifPresent(product::removeVariant);

        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    // ===== TEXTILES =====

    @Transactional
    public ProductDto addTextile(Integer productId, ProductTextileRequest request) {
        Product product = productRepository.findWithDetailsById(productId).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with id `%s` not found".formatted(productId)));

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

        // Создаём связь через метод сущности Product
        product.addTextile(textile, request.percentage());

        // Если в ProductTextileRequest есть primaryFabric — выставляем
        if (request.primaryFabric() != null) {
            product.getTextiles().stream()
                    .filter(pt -> pt.getTextile().getId().equals(textile.getId()))
                    .findFirst()
                    .ifPresent(pt -> pt.setPrimaryFabric(request.primaryFabric()));
        }

        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto removeTextile(Integer productId, Integer textileId) {
        Product product = productRepository.findWithDetailsById(productId).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with id `%s` not found".formatted(productId)));

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
}