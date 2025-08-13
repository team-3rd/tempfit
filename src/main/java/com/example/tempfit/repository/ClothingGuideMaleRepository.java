package com.example.tempfit.repository;

import com.example.tempfit.entity.ClothingGuideMale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClothingGuideMaleRepository extends JpaRepository<ClothingGuideMale, Long> {
    List<ClothingGuideMale> findByTempRange(int tempRange);
}
