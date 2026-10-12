package com.example.clothesstore.color;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "color")
@Getter @Setter @NoArgsConstructor
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;      // "Красный"

    @Column(length = 7)
    private String hex;       // "#FF0000" — для UI

    @Column(nullable = false, length = 20, unique = true)
    private String code;      // "RED" — для SKU
}