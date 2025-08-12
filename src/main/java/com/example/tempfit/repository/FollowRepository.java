package com.example.tempfit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tempfit.entity.Follow;
import com.example.tempfit.entity.Member;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowing(Member follower, Member following);
    void deleteByFollowerAndFollowing(Member follower, Member following);
    Optional<Follow> findByFollowerAndFollowing(Member follower, Member following);
    List<Follow> findByFollower(Member follower);
    List<Follow> findByFollowing(Member following);
    long countByFollower(Member follower);
    long countByFollowing(Member following);
}
