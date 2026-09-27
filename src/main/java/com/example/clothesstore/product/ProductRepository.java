package com.example.clothesstore.product;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    @EntityGraph(attributePaths = {
            "variants", "variants.color", "variants.size",
            "textiles", "textiles.textile"
    })
    Optional<Product> findWithDetailsById(Integer id);

    @EntityGraph(attributePaths = {"variants"})
    List<Product> findAllBy();
}