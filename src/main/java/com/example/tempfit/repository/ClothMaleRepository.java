// package: com.example.tempfit.repository

package com.example.tempfit.repository;

import com.example.tempfit.entity.ClothMale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClothMaleRepository extends JpaRepository<ClothMale, Long> {
    @Query("SELECT c.imageUrl FROM ClothMale c WHERE c.clothName = :clothName")
    String findImageUrlByClothName(@Param("clothName") String clothName);
}
