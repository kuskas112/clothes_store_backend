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
@Profile("dev")
@RequiredArgsConstructor
public class ProductDataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        List<Product> products = List.of(
                // ===== Верхняя одежда =====
                product("Пуховик зимний оверсайз", 12990, Gender.UNISEX, Season.WINTER,
                        "Тёплый пуховик свободного кроя с наполнителем из натурального пуха. "
                                + "Водоотталкивающая ткань, съёмный капюшон и глубокие карманы.\n\n"
                                + "**Состав:** 100% полиэстер, наполнитель — пух/перо 80/20."),
                product("Пальто шерстяное классическое", 15990, Gender.WOMAN, Season.DEMI_SEASON,
                        "Классическое пальто прямого кроя из шерстяной ткани. "
                                + "Подкладка из вискозы, потайная застёжка, два боковых кармана.\n\n"
                                + "**Состав:** 70% шерсть, 30% полиэстер."),
                product("Парка утеплённая с капюшоном", 11490, Gender.UNISEX, Season.WINTER,
                        "Утеплённая парка с длинным капюшоном и ветрозащитной планкой. "
                                + "Подходит для сильных морозов.\n\n"
                                + "**Состав:** 100% нейлон, утеплитель — синтепон."),
                product("Куртка демисезонная стёганая", 8990, Gender.MAN, Season.DEMI_SEASON,
                        "Стёганая куртка на переходный сезон. Лёгкая, тёплая, с воротником-стойкой."),
                product("Ветровка лёгкая с водоотталкиванием", 4290, Gender.UNISEX, Season.SUMMER,
                        "Лёгкая ветровка с водоотталкивающей пропиткой. Складывается в собственный карман."),
                product("Тренч двубортный бежевый", 13990, Gender.WOMAN, Season.DEMI_SEASON,
                        "Двубортный тренч классического бежевого цвета. Пояс в комплекте, "
                                + "подкладка в клетку.\n\n"
                                + "**Состав:** 65% хлопок, 35% полиэстер."),
                product("Кожаная куртка-косуха", 17990, Gender.MAN, Season.DEMI_SEASON,
                        "Косуха из натуральной кожи с асимметричной молнией и косыми карманами."),
                product("Бомбер нейлоновый", 7490, Gender.UNISEX, Season.DEMI_SEASON,
                        "Нейлоновый бомбер с трикотажными манжетами и резинкой по низу."),
                product("Пуховик женский с мехом", 9990, Gender.WOMAN, Season.WINTER,
                        "Женский пуховик с отделкой из натурального меха на капюшоне."),
                product("Куртка мужская демисезонная", 7990, Gender.MAN, Season.DEMI_SEASON,
                        "Мужская демисезонная куртка с утеплителем и водоотталкивающей тканью."),

                // ===== Толстовки и свитшоты =====
                product("Худи oversize с начёсом", 4990, Gender.UNISEX, Season.ALL_SEASON,
                        "Худи свободного кроя с мягким начёсом внутри. Кенгуру-карман, капюшон с шнурком."),
                product("Свитшот хлопковый базовый", 3490, Gender.UNISEX, Season.ALL_SEASON,
                        "Базовый свитшот из плотного хлопка. Универсальная вещь на каждый день."),
                product("Толстовка на молции tech fleece", 5990, Gender.MAN, Season.DEMI_SEASON,
                        "Спортивная толстовка на молнии из технологичного флиса. Дышащая, тёплая."),
                product("Худи с принтом", 3990, Gender.UNISEX, Season.ALL_SEASON,
                        "Худи с ярким принтом на груди. Плотный хлопок, не теряет форму после стирок."),
                product("Свитшот женский укороченный", 3290, Gender.WOMAN, Season.ALL_SEASON,
                        "Укороченный свитшот женского кроя. Отлично сочетается с высокой посадкой."),
                product("Зип-худи с капюшоном", 5490, Gender.UNISEX, Season.DEMI_SEASON,
                        "Худи на молнии с капюшоном и боковыми карманами."),

                // ===== Свитера и кардиганы =====
                product("Свитер крупной вязки", 6990, Gender.UNISEX, Season.WINTER,
                        "Объёмный свитер крупной вязки. Мягкий, тёплый, с удлинёнными рукавами."),
                product("Кардиган длинный шерстяной", 7990, Gender.WOMAN, Season.DEMI_SEASON,
                        "Длинный кардиган из шерстяной пряжи. Носится как пальто в тёплую погоду."),
                product("Джемпер мериносовый", 6490, Gender.MAN, Season.WINTER,
                        "Джемпер из шерсти мериноса. Не колется, сохраняет тепло, дышит."),
                product("Свитер с норвежским узором", 7490, Gender.UNISEX, Season.WINTER,
                        "Свитер с традиционным норвежским орнаментом. Плотная вязка, тёплый."),
                product("Кардиган укороченный хлопковый", 4590, Gender.WOMAN, Season.SUMMER,
                        "Лёгкий хлопковый кардиган на лето. Носится нараспашку или на пуговицы."),
                product("Пуловер кашемировый", 12990, Gender.UNISEX, Season.WINTER,
                        "Пуловер из кашемира. Исключительно мягкий, лёгкий, тёплый."),

                // ===== Футболки и поло =====
                product("Футболка хлопковая базовая", 1490, Gender.UNISEX, Season.SUMMER,
                        "Базовая футболка из 100% хлопка. Плотный трикотаж, прямой крой."),
                product("Футболка oversize с принтом", 1990, Gender.UNISEX, Season.SUMMER,
                        "Футболка свободного кроя с принтом. Хлопок высокой плотности."),
                product("Поло пике классическое", 2790, Gender.MAN, Season.SUMMER,
                        "Классическое поло из пике. Воротник на пуговицах, короткий рукав."),
                product("Футболка женская облегающая", 1690, Gender.WOMAN, Season.SUMMER,
                        "Облегающая женская футболка с эластаном. Подчёркивает фигуру."),
                product("Лонгслив хлопковый", 1990, Gender.UNISEX, Season.DEMI_SEASON,
                        "Хлопковый лонгслив с длинным рукавом. База для многослойных образов."),
                product("Футболка оверсайз без принта", 1790, Gender.UNISEX, Season.SUMMER,
                        "Однотонная футболка оверсайз. Универсальная, подходит под всё."),

                // ===== Рубашки и блузки =====
                product("Рубашка оксфорд классическая", 3990, Gender.MAN, Season.ALL_SEASON,
                        "Классическая рубашка из ткани оксфорд. Прямой крой, воротник на пуговицах."),
                product("Рубашка фланелевая в клетку", 3490, Gender.UNISEX, Season.DEMI_SEASON,
                        "Тёплая фланелевая рубашка в клетку. Мягкая, подходит как верхний слой."),
                product("Блузка женская шёлковая", 4990, Gender.WOMAN, Season.SUMMER,
                        "Шёлковая блузка свободного кроя. Лёгкая, струящаяся, с V-образным вырезом."),
                product("Рубашка джинсовая оверсайз", 4290, Gender.UNISEX, Season.ALL_SEASON,
                        "Джинсовая рубашка оверсайз. Носится как рубашка или как лёгкая куртка."),
                product("Блузка с бантом", 3290, Gender.WOMAN, Season.DEMI_SEASON,
                        "Блузка с декоративным бантом у ворота. Офисный и романтичный вариант."),
                product("Рубашка льняная с коротким рукавом", 3690, Gender.MAN, Season.SUMMER,
                        "Льняная рубашка с коротким рукавом. Дышит, идеальна для жары."),
                product("Блузка женская офисная", 3590, Gender.WOMAN, Season.ALL_SEASON,
                        "Офисная блузка строгого кроя. Подходит под брюки и юбки."),

                // ===== Брюки и джинсы =====
                product("Джинсы прямые классические", 5490, Gender.MAN, Season.ALL_SEASON,
                        "Классические прямые джинсы из плотного денима. Средняя посадка."),
                product("Джинсы скинни женские", 4990, Gender.WOMAN, Season.ALL_SEASON,
                        "Облегающие женские джинсы скинни с эластаном."),
                product("Джинсы mom fit", 5290, Gender.WOMAN, Season.ALL_SEASON,
                        "Джинсы mom fit с высокой посадкой. Свободный крой, подвёрнутый низ."),
                product("Брюки классические со стрелками", 5990, Gender.MAN, Season.ALL_SEASON,
                        "Классические брюки со стрелками. Подходят для делового стиля."),
                product("Брюки женские палаццо", 4690, Gender.WOMAN, Season.SUMMER,
                        "Широкие женские брюки палаццо из лёгкой ткани."),
                product("Чиносы хлопковые", 4290, Gender.MAN, Season.DEMI_SEASON,
                        "Хлопковые чиносы прямого кроя. Универсальны для города и офиса."),
                product("Джоггеры спортивные", 3490, Gender.UNISEX, Season.ALL_SEASON,
                        "Спортивные джоггеры с резинкой на поясе и манжетами."),
                product("Шорты джинсовые", 2990, Gender.UNISEX, Season.SUMMER,
                        "Джинсовые шорты с подвёрнутым низом. Классика летнего гардероба."),
                product("Шорты спортивные", 1990, Gender.MAN, Season.SUMMER,
                        "Лёгкие спортивные шорты с эластичным поясом."),
                product("Легинсы женские плотные", 2490, Gender.WOMAN, Season.ALL_SEASON,
                        "Плотные женские легинсы с высокой посадкой. Не просвечивают."),

                // ===== Платья и юбки =====
                product("Платье вечернее длинное", 11990, Gender.WOMAN, Season.ALL_SEASON,
                        "Вечернее платье в пол. Элегантный крой, струящаяся ткань."),
                product("Платье летнее цветастое", 4290, Gender.WOMAN, Season.SUMMER,
                        "Лёгкое летнее платье с цветочным принтом. Свободный крой, дышащая ткань."),
                product("Платье-свитер вязаное", 6490, Gender.WOMAN, Season.WINTER,
                        "Тёплое вязаное платье-свитер. Носится с ремнём или как есть."),
                product("Сарафан джинсовый", 3990, Gender.WOMAN, Season.SUMMER,
                        "Джинсовый сарафан на бретелях. Универсальная летняя вещь."),
                product("Юбка миди плиссированная", 4290, Gender.WOMAN, Season.DEMI_SEASON,
                        "Плиссированная юбка миди. Лёгкая, подвижная, с эластичным поясом."),
                product("Юбка-карандаш деловая", 3990, Gender.WOMAN, Season.ALL_SEASON,
                        "Деловая юбка-карандаш. Строгий крой, средняя посадка."),
                product("Юбка джинсовая мини", 3290, Gender.WOMAN, Season.SUMMER,
                        "Короткая джинсовая юбка. Классика на лето."),

                // ===== Костюмы и жилеты =====
                product("Костюм спортивный трикотажный", 6990, Gender.UNISEX, Season.DEMI_SEASON,
                        "Спортивный костюм из трикотажа. Куртка на молнии + брюки с резинкой."),
                product("Комбинезон женский летний", 5490, Gender.WOMAN, Season.SUMMER,
                        "Летний женский комбинезон с открытыми плечами и широкими брюками."),
                product("Жилет утеплённый", 4490, Gender.UNISEX, Season.DEMI_SEASON,
                        "Утеплённый жилет. Носится поверх свитера или рубашки."),
                product("Жилет меховой женский", 8990, Gender.WOMAN, Season.WINTER,
                        "Женский меховой жилет. Тёплый, элегантный, с застёжкой на крючки.")
        );

        productRepository.saveAll(products);
        System.out.println(">>> Засеяно продуктов: " + products.size());
    }

    private Product product(String name, double price, Gender gender, Season season, String description) {
        Product p = new Product();
        p.setName(name);
        p.setBasePrice(BigDecimal.valueOf(price));
        p.setGender(gender);
        p.setSeason(season);
        p.setDescription(description);
        return p;
    }
}