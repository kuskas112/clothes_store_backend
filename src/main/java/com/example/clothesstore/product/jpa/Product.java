package com.example.clothesstore.product.jpa;

import com.example.clothesstore.color.Color;
import com.example.clothesstore.size.Size;
import com.example.clothesstore.textile.Textile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductTextile> textiles = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductVariant> variants = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private Set<ProductImage> images = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Season season;

    @Column(precision = 10, scale = 2, name = "base_price")
    private BigDecimal basePrice;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ===== TEXTILES =====

    public void addTextile(Textile textile, int percentage) {
        ProductTextile pt = new ProductTextile();
        pt.setProduct(this);
        pt.setTextile(textile);
        pt.setPercentage(percentage);
        textiles.add(pt);
    }

    public void removeTextile(Textile textile) {
        textiles.removeIf(pt -> pt.getTextile().equals(textile));
    }

    // ===== VARIANTS =====

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.setProduct(null);
    }

    /**
     * Найти вариант по цвету и размеру. Удобно для проверки,
     * существует ли уже такая комбинация перед добавлением.
     */
    public ProductVariant findVariant(Color color, Size size) {
        return variants.stream()
                .filter(v -> java.util.Objects.equals(v.getColor(), color)
                        && java.util.Objects.equals(v.getSize(), size))
                .findFirst()
                .orElse(null);
    }

    // ===== IMAGES =========

    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }
}