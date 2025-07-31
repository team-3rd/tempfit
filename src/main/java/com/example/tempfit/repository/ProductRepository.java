package com.example.tempfit.repository;

import com.example.tempfit.entity.ProductsMale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<ProductsMale, Long> {
    // categoryId(예: "피케/카라티") 기준 전체 상품 조회
    List<ProductsMale> findAllByCategoryId(String categoryId);

    // 중복 방지 등으로 categoryId+imageUrl 조합 존재여부 확인
    boolean existsByCategoryIdAndImageUrl(String categoryId, String imageUrl);
}