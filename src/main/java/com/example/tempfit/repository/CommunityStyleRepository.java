package com.example.tempfit.repository;

import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.CommunityStyle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityStyleRepository extends JpaRepository<CommunityStyle, Long> {
    void deleteByCommunity(Community community);
}
