package com.example.clothesstore.product;

import com.example.clothesstore.image.Image;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "product_variant_image",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_variant_image",
                columnNames = {"variant_id", "image_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ProductVariantImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_main", nullable = false)
    private Boolean isMain = false;

    @Column(name = "alt_text", length = 500)
    private String altText;
}