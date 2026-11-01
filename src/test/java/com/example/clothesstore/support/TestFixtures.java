package com.example.clothesstore.support;

import com.example.clothesstore.category.Category;
import com.example.clothesstore.category.CategoryRepository;
import com.example.clothesstore.color.Color;
import com.example.clothesstore.color.ColorRepository;
import com.example.clothesstore.image.Image;
import com.example.clothesstore.image.ImageRepository;
import com.example.clothesstore.product.jpa.Product;
import com.example.clothesstore.size.Size;
import com.example.clothesstore.size.SizeRepository;
import com.example.clothesstore.size.SizeSystem;
import com.example.clothesstore.textile.Textile;
import com.example.clothesstore.textile.TextileRepository;
import com.example.clothesstore.user.Role;
import com.example.clothesstore.user.User;
import com.example.clothesstore.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Минимальные строители сущностей для тестов: без магии, только обязательные поля.
 */
public final class TestFixtures {

    public static final String DEFAULT_PASSWORD = "password123";

    private TestFixtures() {
    }

    public static Product product(String name) {
        Product product = new Product();
        product.setName(name);
        product.setBasePrice(new BigDecimal("1000.00"));
        return product;
    }

    public static Product product(String name, String basePrice) {
        Product product = new Product();
        product.setName(name);
        product.setBasePrice(new BigDecimal(basePrice));
        return product;
    }

    public static String uniqueName(String prefix) {
        return prefix + " " + UUID.randomUUID();
    }

    public static Image image(ImageRepository repository) {
        Image image = new Image();
        image.setStorageKey("test/" + UUID.randomUUID() + ".jpg");
        image.setContentType("image/jpeg");
        image.setSizeBytes(1L);
        return repository.save(image);
    }

    public static Color color(ColorRepository repository, String code) {
        Color color = new Color();
        color.setCode(code);
        color.setName("Color " + code);
        color.setHex("#112233");
        return repository.save(color);
    }

    public static Size size(SizeRepository repository, String code) {
        Size size = new Size();
        size.setCode(code);
        size.setSystem(SizeSystem.LETTER);
        size.setSortOrder(1);
        return repository.save(size);
    }

    public static Textile textile(TextileRepository repository, String name) {
        Textile textile = new Textile();
        textile.setName(name);
        textile.setPriceModifier(new BigDecimal("1.00"));
        return repository.save(textile);
    }

    public static Category category(CategoryRepository repository, String name, String slug) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        return repository.save(category);
    }

    public static User user(UserRepository repository,
                            PasswordEncoder encoder,
                            String email,
                            Role role) {
        User user = new User(email, encoder.encode(DEFAULT_PASSWORD));
        user.setRole(role);
        return repository.save(user);
    }
}
