package com.example.clothesstore.product.jpa;

import com.example.clothesstore.image.Image;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
        name = "product_image",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_image",
                columnNames = {"product_id", "image_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Column(name = "sort_order", nullable = false)
    @ColumnDefault("0")
    private Integer sortOrder = 0;

    @Column(name = "is_main", nullable = false)
    @ColumnDefault("false")
    private Boolean isMain = false;

    @Column(name = "alt_text", length = 500)
    private String altText;
}