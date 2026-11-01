-- V6__indexes_and_variant_uniqueness.sql
-- 1) Индексы по FK: PostgreSQL не создаёт их автоматически, из-за чего
--    удаление цвета/размера/ткани/картинки или выборка по категории делают seq scan.
-- 2) Уникальность варианта (product, color, size) при NULL-ах: по умолчанию
--    PostgreSQL считает NULL-ы различными, поэтому дубликаты с пустым цветом/размером
--    проходили. NULLS NOT DISTINCT доступен с PostgreSQL 15.

CREATE INDEX IF NOT EXISTS idx_product_textile_textile_id
    ON product_textile (textile_id);

CREATE INDEX IF NOT EXISTS idx_product_variant_color_id
    ON product_variant (color_id);

CREATE INDEX IF NOT EXISTS idx_product_variant_size_id
    ON product_variant (size_id);

CREATE INDEX IF NOT EXISTS idx_product_image_image_id
    ON product_image (image_id);

CREATE INDEX IF NOT EXISTS idx_product_variant_image_image_id
    ON product_variant_image (image_id);

CREATE INDEX IF NOT EXISTS idx_product_category_category_id
    ON product_category (category_id);

CREATE INDEX IF NOT EXISTS idx_category_parent_id
    ON category (parent_id);

ALTER TABLE product_variant
    DROP CONSTRAINT IF EXISTS uk_product_variant_product_color_size;

ALTER TABLE product_variant
    ADD CONSTRAINT uk_product_variant_product_color_size
        UNIQUE NULLS NOT DISTINCT (product_id, color_id, size_id);
