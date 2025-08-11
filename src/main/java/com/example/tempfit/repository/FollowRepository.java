package com.example.tempfit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tempfit.entity.Follow;
import com.example.tempfit.entity.Member;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowing(Member follower, Member following);
    void deleteByFollowerAndFollowing(Member follower, Member following);
    List<Follow> findByFollower(Member follower);
    List<Follow> findByFollowing(Member following);
}
