package com.example.clothesstore.product;

import com.example.clothesstore.category.Category;
import com.example.clothesstore.category.CategoryRepository;
import com.example.clothesstore.product.jpa.Product;
import com.example.clothesstore.product.jpa.ProductCategory;
import com.example.clothesstore.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductCategoryServiceTest {

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product product;
    private Category clothes;
    private Category sale;

    @BeforeEach
    void setUp() {
        product = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Товар")));
        clothes = TestFixtures.category(categoryRepository,
                TestFixtures.uniqueName("Одежда"), TestFixtures.uniqueName("clothes").toLowerCase());
        sale = TestFixtures.category(categoryRepository,
                TestFixtures.uniqueName("Sale"), TestFixtures.uniqueName("sale").toLowerCase());
    }

    @Test
    void addCategoryCreatesLink() {
        productCategoryService.addCategory(product.getId(), clothes.getId(), 0, true);

        List<Category> categories = productCategoryService.getCategoriesForProduct(product.getId());

        assertThat(categories).extracting(Category::getId).containsExactly(clothes.getId());
    }

    @Test
    void duplicateLinkThrowsConflict() {
        productCategoryService.addCategory(product.getId(), clothes.getId(), 0, false);

        assertThatThrownBy(() ->
                productCategoryService.addCategory(product.getId(), clothes.getId(), 1, false))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value()));
    }

    @Test
    void addingSecondPrimaryClearsTheFirst() {
        productCategoryService.addCategory(product.getId(), clothes.getId(), 0, true);
        productCategoryService.addCategory(product.getId(), sale.getId(), 1, true);

        List<ProductCategory> links = productCategoryRepository.findByProductId(product.getId());

        assertThat(links).filteredOn(ProductCategory::getIsPrimary).hasSize(1);
        assertThat(links).filteredOn(ProductCategory::getIsPrimary)
                .extracting(link -> link.getCategory().getId())
                .containsExactly(sale.getId());
    }

    @Test
    void removingPrimaryPromotesRemainingCategory() {
        productCategoryService.addCategory(product.getId(), clothes.getId(), 0, true);
        productCategoryService.addCategory(product.getId(), sale.getId(), 1, false);

        ProductCategory primary = productCategoryRepository.findByProductId(product.getId()).stream()
                .filter(ProductCategory::getIsPrimary)
                .findFirst()
                .orElseThrow();

        productCategoryService.removeCategory(product.getId(), primary.getId());

        List<ProductCategory> links = productCategoryRepository.findByProductId(product.getId());
        assertThat(links).hasSize(1);
        assertThat(links.getFirst().getIsPrimary()).isTrue();
        assertThat(links.getFirst().getCategory().getId()).isEqualTo(sale.getId());
    }

    @Test
    void updateWithWrongProductThrowsBadRequest() {
        Product other = productRepository.save(TestFixtures.product(TestFixtures.uniqueName("Другой")));
        productCategoryService.addCategory(product.getId(), clothes.getId(), 0, false);
        ProductCategory link = productCategoryRepository.findByProductId(product.getId()).getFirst();

        assertThatThrownBy(() ->
                productCategoryService.updateCategory(other.getId(), link.getId(), 5, true))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    void removeUnknownLinkThrows404() {
        assertThatThrownBy(() -> productCategoryService.removeCategory(product.getId(), 999_999L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void updateChangesSortOrderAndPrimaryFlag() {
        productCategoryService.addCategory(product.getId(), clothes.getId(), 0, false);
        productCategoryService.addCategory(product.getId(), sale.getId(), 1, true);
        ProductCategory clothesLink = productCategoryRepository.findByProductId(product.getId()).stream()
                .filter(link -> link.getCategory().getId().equals(clothes.getId()))
                .findFirst()
                .orElseThrow();

        productCategoryService.updateCategory(product.getId(), clothesLink.getId(), 7, true);

        List<ProductCategory> links = productCategoryRepository.findByProductId(product.getId());
        assertThat(links).filteredOn(ProductCategory::getIsPrimary)
                .extracting(link -> link.getCategory().getId())
                .containsExactly(clothes.getId());
        assertThat(links).filteredOn(link -> link.getId().equals(clothesLink.getId()))
                .singleElement()
                .extracting(ProductCategory::getSortOrder)
                .isEqualTo(7);
    }

    @Test
    void categoriesAreReturnedInSortOrder() {
        productCategoryService.addCategory(product.getId(), sale.getId(), 10, false);
        productCategoryService.addCategory(product.getId(), clothes.getId(), 1, false);

        assertThat(productCategoryService.getCategoriesForProduct(product.getId()))
                .extracting(Category::getId)
                .containsExactly(clothes.getId(), sale.getId());
    }
}
