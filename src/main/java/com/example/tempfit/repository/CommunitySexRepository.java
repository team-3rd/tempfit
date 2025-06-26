package com.example.tempfit.repository;

import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.CommunitySex;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunitySexRepository extends JpaRepository<CommunitySex, Long> {
    void deleteByCommunity(Community community);
}
