package com.example.clothesstore.product.size;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "size")
@Getter @Setter @NoArgsConstructor
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Буквенное обозначение: "M", "L", "42" */
    @Column(nullable = false, length = 20)
    private String code;

    /** Отображаемое имя: "Medium", "Средний" */
    @Column(length = 100)
    private String label;

    /** Размерная система: EU, US, UK, LETTER, KIDS */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SizeSystem system;

    /** Порядок для сортировки: XS=1, S=2, M=3... */
    @Column(nullable = false)
    private Integer sortOrder;
}