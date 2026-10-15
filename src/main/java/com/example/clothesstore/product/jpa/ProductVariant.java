package com.example.clothesstore.product.jpa;

import com.example.clothesstore.color.Color;
import com.example.clothesstore.size.Size;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "product_variant",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_variant_sku",
                        columnNames = "sku"
                ),
                @UniqueConstraint(
                        name = "uk_product_variant_product_color_size",
                        columnNames = {"product_id", "color_id", "size_id"}
                )
        }
)
@Getter @Setter @NoArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id")
    private Size size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id")
    private Color color;

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 50, unique = true)
    private String barcode;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private Set<ProductVariantImage> images = new LinkedHashSet<>();

    public void addImage(ProductVariantImage image) {
        images.add(image);
        image.setVariant(this);
    }

    public void removeImage(ProductVariantImage image) {
        images.remove(image);
        image.setVariant(null);
    }
}