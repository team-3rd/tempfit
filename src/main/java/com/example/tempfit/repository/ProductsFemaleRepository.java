package com.example.tempfit.repository;

import com.example.tempfit.entity.ProductsFemale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsFemaleRepository extends JpaRepository<ProductsFemale, Long> {
    List<ProductsFemale> findByCategoryIdIgnoreCase(String categoryId);
}