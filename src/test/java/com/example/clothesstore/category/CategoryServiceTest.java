package com.example.clothesstore.category;

import com.example.clothesstore.exception.CategoryNotFoundException;
import com.example.clothesstore.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void createSetsDefaults() {
        Category category = new Category();
        category.setName(TestFixtures.uniqueName("Аксессуары"));
        category.setSlug(TestFixtures.uniqueName("accessories").toLowerCase());

        Category created = categoryService.create(category);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getActive()).isTrue();
        assertThat(created.getSortOrder()).isZero();
    }

    @Test
    void createWithDuplicateSlugThrows() {
        String slug = TestFixtures.uniqueName("dup-slug").toLowerCase();
        categoryService.create(category(TestFixtures.uniqueName("Первая"), slug));

        assertThatThrownBy(() ->
                categoryService.create(category(TestFixtures.uniqueName("Вторая"), slug)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("slug");
    }

    @Test
    void createWithDuplicateNameThrows() {
        String name = TestFixtures.uniqueName("Обувь");
        categoryService.create(category(name, TestFixtures.uniqueName("shoes").toLowerCase()));

        assertThatThrownBy(() ->
                categoryService.create(category(name, TestFixtures.uniqueName("shoes-2").toLowerCase())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void getByUnknownSlugThrowsNotFound() {
        assertThatThrownBy(() -> categoryService.getBySlug("no-such-slug-" + System.nanoTime()))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void getByUnknownIdThrowsNotFound() {
        assertThatThrownBy(() -> categoryService.getById(999_999L))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void updateAppliesChanges() {
        Category category = categoryService.create(
                category(TestFixtures.uniqueName("Старое"), TestFixtures.uniqueName("old").toLowerCase()));

        Category updated = categoryService.update(category.getId(), target -> {
            target.setName("Новое имя");
            target.setSortOrder(42);
            target.setActive(false);
        });

        assertThat(updated.getName()).isEqualTo("Новое имя");
        assertThat(updated.getSortOrder()).isEqualTo(42);
        assertThat(updated.getActive()).isFalse();
    }

    @Test
    void updateUnknownThrowsNotFound() {
        assertThatThrownBy(() -> categoryService.update(999_999L, target -> target.setName("x")))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void deleteRemovesCategoryAndUnknownIdThrows() {
        Category category = categoryService.create(
                category(TestFixtures.uniqueName("Временная"), TestFixtures.uniqueName("tmp").toLowerCase()));

        categoryService.delete(category.getId());

        assertThat(categoryRepository.findById(category.getId())).isEmpty();
        assertThatThrownBy(() -> categoryService.delete(category.getId()))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void getBySlugReturnsCreatedCategory() {
        String slug = TestFixtures.uniqueName("lookup").toLowerCase();
        Category created = categoryService.create(category(TestFixtures.uniqueName("Поиск"), slug));

        assertThat(categoryService.getBySlug(slug).getId()).isEqualTo(created.getId());
    }

    private static Category category(String name, String slug) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        return category;
    }
}
