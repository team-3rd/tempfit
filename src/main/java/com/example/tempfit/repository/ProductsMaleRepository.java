package com.example.tempfit.repository;

import com.example.tempfit.entity.ProductsMale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsMaleRepository extends JpaRepository<ProductsMale, Long> {
    List<ProductsMale> findByCategoryIdIgnoreCase(String categoryId);
}