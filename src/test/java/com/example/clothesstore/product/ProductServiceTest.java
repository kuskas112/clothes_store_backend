package com.example.clothesstore.product;

import com.example.clothesstore.color.ColorRepository;
import com.example.clothesstore.exception.ProductNotFoundException;
import com.example.clothesstore.image.Image;
import com.example.clothesstore.image.ImageRepository;
import com.example.clothesstore.product.dto.ProductDto;
import com.example.clothesstore.product.dto.ProductImageDto;
import com.example.clothesstore.product.dto.ProductTextileDto;
import com.example.clothesstore.product.dto.request.ProductCreateRequest;
import com.example.clothesstore.product.dto.request.ProductImagePatchRequest;
import com.example.clothesstore.product.dto.request.ProductImageRequest;
import com.example.clothesstore.product.dto.request.ProductPatchRequest;
import com.example.clothesstore.product.dto.request.ProductTextileRequest;
import com.example.clothesstore.product.dto.request.ProductVariantCreateRequest;
import com.example.clothesstore.product.jpa.Gender;
import com.example.clothesstore.product.jpa.Product;
import com.example.clothesstore.product.jpa.Season;
import com.example.clothesstore.size.SizeRepository;
import com.example.clothesstore.support.TestFixtures;
import com.example.clothesstore.textile.Textile;
import com.example.clothesstore.textile.TextileRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private TextileRepository textileRepository;

    @Autowired
    private EntityManager entityManager;

    // ===== CRUD =====

    @Test
    void createPersistsAllFieldsIncludingDiscount() {
        ProductDto created = productService.create(new ProductCreateRequest(
                TestFixtures.uniqueName("Пальто"),
                Gender.WOMAN,
                Season.WINTER,
                new BigDecimal("1000.00"),
                new BigDecimal("800.00"),
                "тёплое"));

        assertThat(created.id()).isNotNull();
        assertThat(created.name()).startsWith("Пальто");
        assertThat(created.basePrice()).isEqualByComparingTo("1000.00");
        assertThat(created.discountedPrice()).isEqualByComparingTo("800.00");
    }

    @Test
    void patchIgnoresNullFields() {
        Product product = productRepository.save(
                TestFixtures.product(TestFixtures.uniqueName("Куртка"), "2000.00"));

        ProductDto updated = productService.update(product.getId(), new ProductPatchRequest(
                null, null, Season.SUMMER, null, new BigDecimal("1500.00"), null));

        assertThat(updated.name()).isEqualTo(product.getName());
        assertThat(updated.basePrice()).isEqualByComparingTo("2000.00");
        assertThat(updated.season()).isEqualTo(Season.SUMMER);
        assertThat(updated.discountedPrice()).isEqualByComparingTo("1500.00");
    }

    @Test
    void updateUnknownProductThrowsNotFound() {
        assertThatThrownBy(() -> productService.update(999_999, new ProductPatchRequest(
                "x", null, null, null, null, null)))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void deleteRemovesProductAndUnknownIdThrows() {
        Product product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Юбка")));

        productService.delete(product.getId());

        assertThat(productRepository.findById(product.getId())).isEmpty();
        assertThatThrownBy(() -> productService.delete(product.getId()))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void getManySkipsUnknownIdsAndKeepsOrder() {
        Product first = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Первая")));
        Product second = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Вторая")));

        var result = productService.getMany(List.of(second.getId(), 999_999, first.getId(), second.getId()));

        assertThat(result).extracting(dto -> dto.id())
                .containsExactly(second.getId(), first.getId());
    }

    // ===== варианты =====

    @Test
    void addVariantLinksColorAndSize() {
        Product product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Худи")));
        var color = TestFixtures.color(colorRepository, "RED");
        var size = TestFixtures.size(sizeRepository, "M");

        ProductDto updated = productService.addVariant(product.getId(),
                new ProductVariantCreateRequest("SKU-" + System.nanoTime(), color.getId(), size.getId(), 5,
                        new BigDecimal("999.00")));

        assertThat(updated.variants()).hasSize(1);
        assertThat(updated.variants().getFirst().color().code()).isEqualTo("RED");
        assertThat(updated.variants().getFirst().size().code()).isEqualTo("M");
        assertThat(updated.variants().getFirst().stockQuantity()).isEqualTo(5);
    }

    @Test
    void addVariantWithUnknownColorThrows404() {
        Product product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Куртка")));

        assertThatThrownBy(() -> productService.addVariant(product.getId(),
                new ProductVariantCreateRequest("SKU-" + System.nanoTime(), 999_999, null, 1, null)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void removeUnknownVariantThrows404() {
        Product product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Парка")));

        assertThatThrownBy(() -> productService.removeVariant(product.getId(), 424242))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    // ===== изображения =====

    @Test
    void addImageAssignsIncrementingSortOrder() {
        Product product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Тренч")));
        Image first = TestFixtures.image(imageRepository);
        Image second = TestFixtures.image(imageRepository);

        productService.addImage(product.getId(), new ProductImageRequest(first.getId(), null, null, null));
        ProductDto updated = productService.addImage(product.getId(),
                new ProductImageRequest(second.getId(), null, null, null));

        assertThat(updated.images()).hasSize(2);
        assertThat(updated.images()).extracting(ProductImageDto::sortOrder)
                .containsExactly(0, 1);
    }

    @Test
    void addSameImageTwiceThrowsConflict() {
        Product product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Бомбер")));
        Image image = TestFixtures.image(imageRepository);
        productService.addImage(product.getId(), new ProductImageRequest(image.getId(), null, null, true));

        assertThatThrownBy(() -> productService.addImage(product.getId(),
                new ProductImageRequest(image.getId(), null, null, null)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value()));
    }

    @Test
    void onlyOneImageCanBeMain() {
        Product product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Свитер")));
        Image first = TestFixtures.image(imageRepository);
        Image second = TestFixtures.image(imageRepository);

        productService.addImage(product.getId(), new ProductImageRequest(first.getId(), null, null, true));
        ProductDto updated = productService.addImage(product.getId(),
                new ProductImageRequest(second.getId(), null, null, true));

        assertThat(updated.images()).filteredOn(ProductImageDto::isMain).hasSize(1);
        assertThat(updated.images()).filteredOn(ProductImageDto::isMain)
                .extracting(ProductImageDto::id)
                .containsExactly(updated.images().stream()
                        .filter(pi -> pi.image().id().equals(second.getId()))
                        .findFirst().orElseThrow().id());
    }

    @Test
    void removingMainImagePromotesAnother() {
        Product product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Жилет")));
        Image first = TestFixtures.image(imageRepository);
        Image second = TestFixtures.image(imageRepository);
        ProductDto withFirstMain = productService.addImage(product.getId(),
                new ProductImageRequest(first.getId(), null, 0, true));
        ProductDto withBoth = productService.addImage(product.getId(),
                new ProductImageRequest(second.getId(), null, 1, false));

        Long mainId = withBoth.images().stream().filter(ProductImageDto::isMain)
                .findFirst().orElseThrow().id();
        ProductDto afterRemoval = productService.removeImage(product.getId(), mainId);

        assertThat(afterRemoval.images()).hasSize(1);
        assertThat(afterRemoval.images().getFirst().isMain()).isTrue();
        assertThat(withFirstMain.images()).hasSize(1);
    }

    @Test
    void updateImageCanSwitchMainFlag() {
        Product product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Костюм")));
        Image first = TestFixtures.image(imageRepository);
        Image second = TestFixtures.image(imageRepository);
        productService.addImage(product.getId(), new ProductImageRequest(first.getId(), null, 0, true));
        ProductDto dto = productService.addImage(product.getId(),
                new ProductImageRequest(second.getId(), null, 1, false));

        Long secondImageId = dto.images().stream()
                .filter(pi -> pi.image().id().equals(second.getId()))
                .findFirst().orElseThrow().id();

        ProductDto updated = productService.updateImage(product.getId(), secondImageId,
                new ProductImagePatchRequest("alt", 10, true));

        assertThat(updated.images()).filteredOn(ProductImageDto::isMain).hasSize(1);
        assertThat(updated.images()).filteredOn(pi -> pi.id().equals(secondImageId))
                .singleElement()
                .satisfies(pi -> {
                    assertThat(pi.isMain()).isTrue();
                    assertThat(pi.sortOrder()).isEqualTo(10);
                });
    }

    // ===== ткани =====

    @Test
    void addTextileRejectsDuplicateLink() {
        Product product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Рубашка")));
        Textile textile = TestFixtures.textile(textileRepository, TestFixtures.uniqueName("Лён"));

        productService.addTextile(product.getId(), new ProductTextileRequest(textile.getId(), 60, true));

        assertThatThrownBy(() -> productService.addTextile(product.getId(),
                new ProductTextileRequest(textile.getId(), 40, false)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value()));
    }

    @Test
    void onlyOneTextileCanBePrimary() {
        Product product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Блузка")));
        Textile cotton = TestFixtures.textile(textileRepository, TestFixtures.uniqueName("Хлопок"));
        Textile silk = TestFixtures.textile(textileRepository, TestFixtures.uniqueName("Шёлк"));

        productService.addTextile(product.getId(), new ProductTextileRequest(cotton.getId(), 60, true));
        ProductDto updated = productService.addTextile(product.getId(),
                new ProductTextileRequest(silk.getId(), 40, true));

        assertThat(updated.textiles()).filteredOn(ProductTextileDto::primaryFabric).hasSize(1);
        assertThat(updated.textiles()).filteredOn(ProductTextileDto::primaryFabric)
                .extracting(ProductTextileDto::textileId)
                .containsExactly(silk.getId());
    }

    @Test
    void removeTextileThatIsNotLinkedThrows404() {
        Product product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Шорты")));

        assertThatThrownBy(() -> productService.removeTextile(product.getId(), 999_999))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void removeTextileRemovesLink() {
        Product product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Джинсы")));
        Textile textile = TestFixtures.textile(textileRepository, TestFixtures.uniqueName("Деним"));
        ProductDto withTextile = productService.addTextile(product.getId(),
                new ProductTextileRequest(textile.getId(), 100, null));

        ProductDto updated = productService.removeTextile(product.getId(), textile.getId());

        assertThat(withTextile.textiles()).hasSize(1);
        assertThat(updated.textiles()).isEmpty();
    }

    @Test
    void imagesAreReturnedSortedBySortOrder() {
        Product product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Платье")));
        Image first = TestFixtures.image(imageRepository);
        Image second = TestFixtures.image(imageRepository);
        productService.addImage(product.getId(), new ProductImageRequest(first.getId(), null, 5, null));
        productService.addImage(product.getId(),
                new ProductImageRequest(second.getId(), null, 1, null));

        // @OrderBy применяется при загрузке из БД, поэтому перечитываем сущность.
        entityManager.flush();
        entityManager.clear();

        ProductDto dto = productService.getOne(product.getId());
        assertThat(dto.images()).isSortedAccordingTo(Comparator.comparing(ProductImageDto::sortOrder));
    }
}
