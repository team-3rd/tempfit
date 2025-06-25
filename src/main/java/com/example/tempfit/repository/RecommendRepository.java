package com.example.tempfit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Recommend;

public interface RecommendRepository extends JpaRepository<Recommend, Long>  {
    boolean existsByMemberAndCommunity(Member member, Community community);

    Optional<Recommend> findByMemberAndCommunity(Member member, Community community);
}
