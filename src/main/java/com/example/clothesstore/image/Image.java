package com.example.clothesstore.image;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "image",
        indexes = {
                @Index(name = "idx_image_storage_key", columnList = "storage_key", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ключ объекта в хранилище (S3/MinIO) или относительный путь на диске.
     * Пример: "products/2024/11/abc-123.jpg"
     * Именно это, а не полный URL, храним в БД — чтобы можно было менять CDN/бакет
     * без миграции данных.
     */
    @Column(name = "storage_key", nullable = false, unique = true, length = 500)
    private String storageKey;

    /**
     * Оригинальное имя файла, как загрузил пользователь. Нужно для скачивания
     * и для отладки. Не для отображения.
     */
    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    /**
     * MIME-тип: image/jpeg, image/png, image/webp...
     */
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    /**
     * Размер файла в байтах. Полезно для аналитики и для лимитов.
     */
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    /**
     * Ширина и высота в пикселях. Нужны, чтобы фронт мог зарезервировать место
     * и не дёргался layout. Могут быть null, если не удалось определить.
     */
    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    /**
     * Alt-текст по умолчанию. Может переопределяться на уровне связи
     * (ProductImage.altText), если для конкретного места нужен другой.
     */
    @Column(name = "alt_text", length = 500)
    private String altText;

    /**
     * Контрольная сумма (например, SHA-256). Позволяет:
     * - дедуплицировать одинаковые файлы,
     * - проверять целостность.
     * Необязательно, но полезно.
     */
    @Column(name = "checksum", length = 64, unique = true)
    private String checksum;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}