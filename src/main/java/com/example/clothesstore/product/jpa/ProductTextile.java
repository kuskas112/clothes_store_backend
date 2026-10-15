package com.example.clothesstore.product.jpa;

import com.example.clothesstore.textile.Textile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "product_textile",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_textile",
                columnNames = {"product_id", "textile_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ProductTextile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "textile_id", nullable = false)
    private Textile textile;

    // поля самой связи:
    private Integer percentage;       // сколько процентов ткани в продукте
    private Boolean primaryFabric;    // основная ткань или нет

    // ===== equals / hashCode =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductTextile other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}