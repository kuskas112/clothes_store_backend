package com.example.clothesstore.textile;

import com.example.clothesstore.product.ProductTextile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
        name = "textile",
        uniqueConstraints = @UniqueConstraint(name = "uk_textile_name", columnNames = "name")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"description", "careInstructions"})
public class Textile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "composition", length = 255)
    private String composition;

    @Column(name = "density_gsm")
    private Integer densityGsm;

    @Enumerated(EnumType.STRING)
    @Column(name = "weave_type", length = 30)
    private WeaveType weaveType;

    @Enumerated(EnumType.STRING)
    @Column(name = "stretch", length = 20)
    private Stretch stretch;

    @Enumerated(EnumType.STRING)
    @Column(name = "manufacturer", length = 20)
    private Manufacturer manufacturer;

    @Column(name = "care_instructions", length = 500)
    private String careInstructions;

    @Column(name = "price_modifier", precision = 10, scale = 2)
    private BigDecimal priceModifier;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "textile")
    private Set<ProductTextile> products = new HashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> objectEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != objectEffectiveClass) {
            return false;
        }
        Textile textile = (Textile) o;
        return getId() != null && Objects.equals(getId(), textile.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}