package com.example.clothesstore.product;

import com.example.clothesstore.image.Image;
import com.example.clothesstore.image.ImageRepository;
import com.example.clothesstore.product.dto.ProductShortDto;
import com.example.clothesstore.product.jpa.Product;
import com.example.clothesstore.product.jpa.ProductImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверяет список товаров после исправления N+1 и fallback для превью.
 */
@SpringBootTest
@ActiveProfiles("test")
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageRepository imageRepository;

    @BeforeEach
    void cleanUp() {
        productRepository.deleteAll();
        imageRepository.deleteAll();
    }

    @Test
    void previewUsesImageMarkedAsMain() {
        Image first = image("first.jpg");
        Image second = image("second.jpg");
        product("Пальто", first, false, second, true);

        Page<ProductShortDto> page = productService.getAll(
                PageRequest.of(0, 10, Sort.by("id")));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().previewImageUrl()).endsWith("/" + second.getStorageKey());
    }

    @Test
    void previewFallsBackToFirstImageWhenMainIsNotSet() {
        Image first = image("first.jpg");
        Image second = image("second.jpg");
        product("Куртка", first, false, second, false);

        Page<ProductShortDto> page = productService.getAll(
                PageRequest.of(0, 10, Sort.by("id")));

        assertThat(page.getContent().getFirst().previewImageUrl()).endsWith("/" + first.getStorageKey());
    }

    @Test
    void productWithoutImagesHasNullPreview() {
        product("Без картинок", null, false, null, false);

        Page<ProductShortDto> page = productService.getAll(
                PageRequest.of(0, 10, Sort.by("id")));

        assertThat(page.getContent().getFirst().previewImageUrl()).isNull();
    }

    @Test
    void getManyPreservesRequestedOrder() {
        Product one = product("Первый", null, false, null, false);
        Product two = product("Второй", null, false, null, false);

        List<ProductShortDto> result = productService.getMany(List.of(two.getId(), one.getId()));

        assertThat(result).extracting(ProductShortDto::name)
                .containsExactly("Второй", "Первый");
    }

    private Image image(String name) {
        Image image = new Image();
        image.setStorageKey("test/" + UUID.randomUUID() + "-" + name);
        image.setContentType("image/jpeg");
        image.setSizeBytes(1L);
        return imageRepository.save(image);
    }

    private Product product(String name,
                            Image first,
                            boolean firstIsMain,
                            Image second,
                            boolean secondIsMain) {
        Product product = new Product();
        product.setName(name);
        product.setBasePrice(new BigDecimal("100.00"));

        if (first != null) {
            ProductImage link = new ProductImage();
            link.setImage(first);
            link.setSortOrder(0);
            link.setIsMain(firstIsMain);
            product.addImage(link);
        }
        if (second != null) {
            ProductImage link = new ProductImage();
            link.setImage(second);
            link.setSortOrder(1);
            link.setIsMain(secondIsMain);
            product.addImage(link);
        }

        return productRepository.saveAndFlush(product);
    }
}
