package com.example.clothesstore.dataseeders;

import com.example.clothesstore.textile.Manufacturer;
import com.example.clothesstore.textile.Stretch;
import com.example.clothesstore.textile.Textile;
import com.example.clothesstore.textile.TextileRepository;
import com.example.clothesstore.textile.WeaveType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Profile("dev")
@Order(1) // запустится раньше ProductDataSeeder
@RequiredArgsConstructor
public class TextileDataSeeder implements CommandLineRunner {

    private final TextileRepository textileRepository;

    @Override
    public void run(String... args) {
        if (textileRepository.count() > 0) {
            return; // уже засеяно
        }

        List<Textile> textiles = List.of(
                // ===== Натуральные =====
                textile("Хлопок", "Натуральная мягкая ткань, дышит, гипоаллергенна",
                        "100% хлопок", 140, WeaveType.WOVEN, Stretch.NONE, Manufacturer.RUSSIA,
                        "Машинная стирка 40°C, не отбеливать", "1.00"),

                textile("Лён", "Натуральная ткань с характерной фактурой, хорошо пропускает воздух",
                        "100% лён", 200, WeaveType.WOVEN, Stretch.NONE, Manufacturer.BELARUS,
                        "Машинная стирка 30°C, гладить влажным", "1.15"),

                textile("Шерсть мериноса", "Тонкая шерсть, тёплая, не колется",
                        "100% шерсть мериноса", 280, WeaveType.KNITTED, Stretch.MEDIUM, Manufacturer.RUSSIA,
                        "Ручная стирка 30°C или химчистка", "1.80"),

                textile("Шёлк", "Гладкая блестящая ткань премиум-класса",
                        "100% шёлк", 90, WeaveType.WOVEN, Stretch.NONE, Manufacturer.CHINA,
                        "Только ручная стирка или химчистка", "2.50"),

                textile("Кашемир", "Очень мягкая и тёплая шерсть премиум-класса",
                        "100% кашемир", 320, WeaveType.KNITTED, Stretch.MEDIUM, Manufacturer.CHINA,
                        "Ручная стирка 30°C, сушить горизонтально", "3.20"),

                // ===== Синтетика =====
                textile("Полиэстер", "Прочная синтетическая ткань, устойчива к сминанию",
                        "100% полиэстер", 180, WeaveType.WOVEN, Stretch.LOW, Manufacturer.CHINA,
                        "Машинная стирка 40°C, не гладить горячим", "0.80"),

                textile("Нейлон", "Лёгкая прочная синтетика, часто используется в куртках",
                        "100% нейлон", 120, WeaveType.WOVEN, Stretch.MEDIUM, Manufacturer.CHINA,
                        "Машинная стирка 30°C, не отжимать", "0.95"),

                textile("Эластан (спандекс)", "Добавляется для эластичности",
                        "95% полиэстер, 5% эластан", 220, WeaveType.KNITTED, Stretch.HIGH, Manufacturer.TURKEY,
                        "Машинная стирка 30°C, низкая температура глажки", "1.20"),

                textile("Акрил", "Синтетический аналог шерсти, мягкий и тёплый",
                        "100% акрил", 260, WeaveType.KNITTED, Stretch.MEDIUM, Manufacturer.TURKEY,
                        "Машинная стирка 30°C, не гладить", "0.90"),

                // ===== Смесовые =====
                textile("Хлопок-эластан", "Хлопок с добавлением эластана для лучшей посадки",
                        "95% хлопок, 5% эластан", 180, WeaveType.KNITTED, Stretch.HIGH, Manufacturer.TURKEY,
                        "Машинная стирка 40°C", "1.10"),

                textile("Шерсть-акрил", "Смесовая тёплая ткань, доступнее чистой шерсти",
                        "50% шерсть, 50% акрил", 300, WeaveType.KNITTED, Stretch.MEDIUM, Manufacturer.BELARUS,
                        "Машинная стирка 30°C, деликатный режим", "1.40"),

                textile("Твил (джинсовая)", "Плотная хлопковая ткань саржевого плетения",
                        "98% хлопок, 2% эластан", 340, WeaveType.WOVEN, Stretch.LOW, Manufacturer.TURKEY,
                        "Машинная стирка 40°C, стирать отдельно первые разы", "1.30"),

                // ===== Технические / функциональные =====
                textile("Мембрана", "Водонепроницаемая дышащая ткань для верхней одежды",
                        "100% полиэстер с PU-мембраной", 150, WeaveType.NON_WOVEN, Stretch.LOW, Manufacturer.CHINA,
                        "Машинная стирка 30°C без кондиционера", "2.20"),

                textile("Флис", "Тёплый лёгкий синтетический материал с начёсом",
                        "100% полиэстер", 240, WeaveType.KNITTED, Stretch.MEDIUM, Manufacturer.CHINA,
                        "Машинная стирка 40°C", "1.05"),

                textile("Софтшелл", "Эластичная мембранная ткань с мягкой изнанкой",
                        "94% полиэстер, 6% эластан", 210, WeaveType.WOVEN, Stretch.HIGH, Manufacturer.CHINA,
                        "Машинная стирка 30°C, не использовать кондиционер", "1.90"),

                // ===== Утеплители =====
                textile("Пух-перо", "Натуральный утеплитель, очень тёплый и лёгкий",
                        "90% пух, 10% перо", 80, WeaveType.NON_WOVEN, Stretch.NONE, Manufacturer.RUSSIA,
                        "Химчистка или стирка с теннисными мячиками", "3.00"),

                textile("Синтепух", "Синтетический аналог пуха, гипоаллергенный",
                        "100% полиэстер", 200, WeaveType.NON_WOVEN, Stretch.NONE, Manufacturer.BELARUS,
                        "Машинная стирка 40°C", "1.50"),

                textile("Тинсулейт", "Тонкий высокоэффективный синтетический утеплитель",
                        "100% полиэстер", 100, WeaveType.NON_WOVEN, Stretch.LOW, Manufacturer.RUSSIA,
                        "Машинная стирка 30°C", "2.80"),

                // ===== Трикотаж =====
                textile("Кулирка", "Тонкий хлопковый трикотаж для футболок",
                        "100% хлопок", 160, WeaveType.KNITTED, Stretch.HIGH, Manufacturer.TURKEY,
                        "Машинная стирка 40°C", "0.90"),

                textile("Футер", "Плотный трикотаж с начёсом или петлёй для худи",
                        "80% хлопок, 20% полиэстер", 300, WeaveType.KNITTED, Stretch.MEDIUM, Manufacturer.TURKEY,
                        "Машинная стирка 40°C, стирать наизнанку", "1.25"),

                textile("Джерси", "Эластичный трикотаж, хорошо драпируется",
                        "95% вискоза, 5% эластан", 180, WeaveType.KNITTED, Stretch.HIGH, Manufacturer.TURKEY,
                        "Машинная стирка 30°C", "1.15"),

                textile("Рибанa", "Трикотаж в резинку, используется для манжет и воротников",
                        "95% хлопок, 5% эластан", 220, WeaveType.KNITTED, Stretch.HIGH, Manufacturer.TURKEY,
                        "Машинная стирка 40°C", "1.00"),

                // ===== Кружево и декор =====
                textile("Кружево", "Ажурная ткань для декора и отделки",
                        "100% полиэстер", 60, WeaveType.LACE, Stretch.LOW, Manufacturer.CHINA,
                        "Ручная стирка 30°C", "1.70"),

                textile("Войлок", "Плотный нетканый материал из шерсти",
                        "100% шерсть", 400, WeaveType.FELT, Stretch.NONE, Manufacturer.RUSSIA,
                        "Химчистка", "1.60"),

                textile("Вискоза", "Мягкая ткань с шелковистой поверхностью, искусственного происхождения",
                        "100% вискоза", 130, WeaveType.WOVEN, Stretch.LOW, Manufacturer.CHINA,
                        "Машинная стирка 30°C, не отжимать", "0.85")
        );

        textileRepository.saveAll(textiles);
        System.out.println(">>> Засеяно тканей: " + textiles.size());
    }

    private Textile textile(String name,
                            String description,
                            String composition,
                            int densityGsm,
                            WeaveType weaveType,
                            Stretch stretch,
                            Manufacturer manufacturer,
                            String careInstructions,
                            String priceModifier) {
        Textile t = new Textile();
        t.setName(name);
        t.setDescription(description);
        t.setComposition(composition);
        t.setDensityGsm(densityGsm);
        t.setWeaveType(weaveType);
        t.setStretch(stretch);
        t.setManufacturer(manufacturer);
        t.setCareInstructions(careInstructions);
        t.setPriceModifier(new BigDecimal(priceModifier));
        t.setActive(true);
        return t;
    }
}