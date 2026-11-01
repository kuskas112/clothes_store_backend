-- V7__add_product_discounts.sql
--
-- Скидки (discounted_price) появились в коде коммитом "add discounts", но колонка
-- была дописана прямо в V1__init.sql — то есть в уже применённую миграцию.
-- На БД, где V1 применилась раньше, колонки нет. Добавляем её отдельной миграцией.
--
-- IF NOT EXISTS делает миграцию безопасной и там, где discounted_price уже есть
-- (V1 применена в новой редакции): повторного добавления не произойдёт.
--
-- Если при старте Flyway ругается на checksum V1 (потому что файл V1 правили),
-- один раз выполните repair — например, через Flyway CLI:
--   flyway repair -url=jdbc:postgresql://localhost:5432/oltati \
--                 -user=oltati_user -password=... \
--                 -locations=filesystem:/path/to/src/main/resources/db/migration
-- После repair Flyway обновит контрольные суммы и применит V7.

ALTER TABLE product
    ADD COLUMN IF NOT EXISTS discounted_price numeric(10, 2);

COMMENT ON COLUMN product.discounted_price IS
    'Цена со скидкой; NULL — скидки нет';

-- Скидка не может быть отрицательной или больше базовой цены.
-- NOT VALID: исторические строки не проверяются (миграция не упадёт на старых
-- данных), но все новые INSERT/UPDATE проверяются.
ALTER TABLE product
    DROP CONSTRAINT IF EXISTS ck_product_discounted_price;

ALTER TABLE product
    ADD CONSTRAINT ck_product_discounted_price
        CHECK (
            discounted_price IS NULL
            OR (discounted_price >= 0
                AND (base_price IS NULL OR discounted_price <= base_price))
        ) NOT VALID;

-- Ускоряет фильтр «товары со скидкой» (категория Sale, сортировка по размеру скидки).
CREATE INDEX IF NOT EXISTS idx_product_discounted_price
    ON product (discounted_price)
    WHERE discounted_price IS NOT NULL;
