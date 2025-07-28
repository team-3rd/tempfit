package com.example.tempfit.repository;

import com.example.tempfit.entity.Product;
import com.example.tempfit.entity.Sex;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllBySexAndItemName(Sex sex, String itemName);
    boolean existsByItemNameAndImageUrl(String itemName, String imageUrl);
}