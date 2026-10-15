package com.example.clothesstore.category;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "category",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_slug", columnNames = "slug"),
                @UniqueConstraint(name = "uk_category_name", columnNames = "name")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String slug;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private Boolean active = true;
}