package com.example.tempfit.repository;

import com.example.tempfit.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsRepository extends JpaRepository<Products, Long> {
    List<Products> findByGenderAndCategoryIdIgnoreCase(Integer gender, String categoryId);
    Products findByProductId(Long productId);
}
