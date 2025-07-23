package com.example.tempfit.repository;

import com.example.tempfit.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByProductKey(String productKey);
    boolean existsByProductKeyAndImageUrl(String productKey, String imageUrl);
}