package com.example.clothesstore.product;

import com.example.clothesstore.product.jpa.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findByProductId(Integer productId);

    List<ProductCategory> findByProductIdOrderBySortOrderAscIdAsc(Integer productId);

    List<ProductCategory> findByCategoryId(Long categoryId);

    boolean existsByProductIdAndCategoryId(Integer productId, Long categoryId);

    void deleteByProductIdAndCategoryId(Integer productId, Long categoryId);

    @Query("""
        select pc from ProductCategory pc
        join fetch pc.category
        where pc.product.id in :productIds
        order by pc.product.id, pc.sortOrder, pc.id
        """)
    List<ProductCategory> findByProductIdIn(@Param("productIds") List<Integer> productIds);

    void deleteByProductId(Integer productId);

    void deleteByProductIdIn(List<Integer> productIds);
}