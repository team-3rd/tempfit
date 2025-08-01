package com.example.tempfit.repository;

import com.example.tempfit.entity.ClothFemale;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClothFemaleRepository extends JpaRepository<ClothFemale, Long> {
    @Query("SELECT c.imageUrl FROM ClothFemale c WHERE c.clothName = :clothName")
    String findImageUrlByClothName(@Param("clothName") String clothName);
    Optional<ClothFemale> findByClothNameIgnoreCase(String clothName);
}
