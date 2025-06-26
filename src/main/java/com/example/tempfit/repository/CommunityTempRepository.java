package com.example.tempfit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tempfit.entity.CommunityTemp;

public interface CommunityTempRepository extends JpaRepository<CommunityTemp, Long> {

}
