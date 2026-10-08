package com.example.clothesstore.dataseeders;

import com.example.clothesstore.product.Gender;
import com.example.clothesstore.product.Product;
import com.example.clothesstore.product.ProductRepository;
import com.example.clothesstore.product.Season;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("dev") // только для dev-профиля
@RequiredArgsConstructor
public class ProductDataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return; // уже засеяно
        }

        List<Product> products = List.of(
                // ===== Верхняя одежда =====
                product("Пуховик зимний оверсайз", 12990, Gender.UNISEX, Season.WINTER),
                product("Пальто шерстяное классическое", 15990, Gender.WOMAN, Season.DEMI_SEASON),
                product("Парка утеплённая с капюшоном", 11490, Gender.UNISEX, Season.WINTER),
                product("Куртка демисезонная стёганая", 8990, Gender.MAN, Season.DEMI_SEASON),
                product("Ветровка лёгкая с водоотталкиванием", 4290, Gender.UNISEX, Season.SUMMER),
                product("Тренч двубортный бежевый", 13990, Gender.WOMAN, Season.DEMI_SEASON),
                product("Кожаная куртка-косуха", 17990, Gender.MAN, Season.DEMI_SEASON),
                product("Бомбер нейлоновый", 7490, Gender.UNISEX, Season.DEMI_SEASON),
                product("Пуховик женский с мехом", 9990, Gender.WOMAN, Season.WINTER),
                product("Куртка мужская демисезонная", 7990, Gender.MAN, Season.DEMI_SEASON),

                // ===== Толстовки и свитшоты =====
                product("Худи oversize с начёсом", 4990, Gender.UNISEX, Season.ALL_SEASON),
                product("Свитшот хлопковый базовый", 3490, Gender.UNISEX, Season.ALL_SEASON),
                product("Толстовка на молнии tech fleece", 5990, Gender.MAN, Season.DEMI_SEASON),
                product("Худи с принтом", 3990, Gender.UNISEX, Season.ALL_SEASON),
                product("Свитшот женский укороченный", 3290, Gender.WOMAN, Season.ALL_SEASON),
                product("Зип-худи с капюшоном", 5490, Gender.UNISEX, Season.DEMI_SEASON),

                // ===== Свитера и кардиганы =====
                product("Свитер крупной вязки", 6990, Gender.UNISEX, Season.WINTER),
                product("Кардиган длинный шерстяной", 7990, Gender.WOMAN, Season.DEMI_SEASON),
                product("Джемпер мериносовый", 6490, Gender.MAN, Season.WINTER),
                product("Свитер с норвежским узором", 7490, Gender.UNISEX, Season.WINTER),
                product("Кардиган укороченный хлопковый", 4590, Gender.WOMAN, Season.SUMMER),
                product("Пуловер кашемировый", 12990, Gender.UNISEX, Season.WINTER),

                // ===== Футболки и поло =====
                product("Футболка хлопковая базовая", 1490, Gender.UNISEX, Season.SUMMER),
                product("Футболка oversize с принтом", 1990, Gender.UNISEX, Season.SUMMER),
                product("Поло пике классическое", 2790, Gender.MAN, Season.SUMMER),
                product("Футболка женская облегающая", 1690, Gender.WOMAN, Season.SUMMER),
                product("Лонгслив хлопковый", 1990, Gender.UNISEX, Season.DEMI_SEASON),
                product("Футболка оверсайз без принта", 1790, Gender.UNISEX, Season.SUMMER),

                // ===== Рубашки и блузки =====
                product("Рубашка оксфорд классическая", 3990, Gender.MAN, Season.ALL_SEASON),
                product("Рубашка фланелевая в клетку", 3490, Gender.UNISEX, Season.DEMI_SEASON),
                product("Блузка женская шёлковая", 4990, Gender.WOMAN, Season.SUMMER),
                product("Рубашка джинсовая оверсайз", 4290, Gender.UNISEX, Season.ALL_SEASON),
                product("Блузка с бантом", 3290, Gender.WOMAN, Season.DEMI_SEASON),
                product("Рубашка льняная с коротким рукавом", 3690, Gender.MAN, Season.SUMMER),
                product("Блузка женская офисная", 3590, Gender.WOMAN, Season.ALL_SEASON),

                // ===== Брюки и джинсы =====
                product("Джинсы прямые классические", 5490, Gender.MAN, Season.ALL_SEASON),
                product("Джинсы скинни женские", 4990, Gender.WOMAN, Season.ALL_SEASON),
                product("Джинсы mom fit", 5290, Gender.WOMAN, Season.ALL_SEASON),
                product("Брюки классические со стрелками", 5990, Gender.MAN, Season.ALL_SEASON),
                product("Брюки женские палаццо", 4690, Gender.WOMAN, Season.SUMMER),
                product("Чиносы хлопковые", 4290, Gender.MAN, Season.DEMI_SEASON),
                product("Джоггеры спортивные", 3490, Gender.UNISEX, Season.ALL_SEASON),
                product("Шорты джинсовые", 2990, Gender.UNISEX, Season.SUMMER),
                product("Шорты спортивные", 1990, Gender.MAN, Season.SUMMER),
                product("Легинсы женские плотные", 2490, Gender.WOMAN, Season.ALL_SEASON),

                // ===== Платья и юбки =====
                product("Платье вечернее длинное", 11990, Gender.WOMAN, Season.ALL_SEASON),
                product("Платье летнее цветастое", 4290, Gender.WOMAN, Season.SUMMER),
                product("Платье-свитер вязаное", 6490, Gender.WOMAN, Season.WINTER),
                product("Сарафан джинсовый", 3990, Gender.WOMAN, Season.SUMMER),
                product("Юбка миди плиссированная", 4290, Gender.WOMAN, Season.DEMI_SEASON),
                product("Юбка-карандаш деловая", 3990, Gender.WOMAN, Season.ALL_SEASON),
                product("Юбка джинсовая мини", 3290, Gender.WOMAN, Season.SUMMER),

                // ===== Костюмы и жилеты =====
                product("Костюм спортивный трикотажный", 6990, Gender.UNISEX, Season.DEMI_SEASON),
                product("Комбинезон женский летний", 5490, Gender.WOMAN, Season.SUMMER),
                product("Жилет утеплённый", 4490, Gender.UNISEX, Season.DEMI_SEASON),
                product("Жилет меховой женский", 8990, Gender.WOMAN, Season.WINTER)
        );

        productRepository.saveAll(products);
        System.out.println(">>> Засеяно продуктов: " + products.size());
    }

    private Product product(String name, double price, Gender gender, Season season) {
        Product p = new Product();
        p.setName(name);
        p.setBasePrice(BigDecimal.valueOf(price));
        p.setGender(gender);
        p.setSeason(season);
        return p;
    }
}