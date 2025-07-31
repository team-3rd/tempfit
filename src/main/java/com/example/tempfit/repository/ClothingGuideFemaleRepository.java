package com.example.tempfit.repository;

import com.example.tempfit.entity.ClothingGuideFemale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClothingGuideFemaleRepository extends JpaRepository<ClothingGuideFemale, Long> {
    List<ClothingGuideFemale> findByTempRange(int tempRange);
}
