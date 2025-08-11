package com.example.tempfit.service;

import org.springframework.stereotype.Service;

import com.example.tempfit.entity.Follow;
import com.example.tempfit.entity.Member;
import com.example.tempfit.repository.FollowRepository;
import com.example.tempfit.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    public void follow(String followerEmail, String followingEmail) {
        Member follower = memberRepository.findById(followerEmail)
                .orElseThrow(() -> new RuntimeException("팔로우 하는 회원 없음"));
        Member following = memberRepository.findById(followingEmail)
                .orElseThrow(() -> new RuntimeException("팔로우 받는 회원 없음"));

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new RuntimeException("이미 팔로우 중");
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);
    }

    public void unfollow(String followerEmail, String followingEmail) {
        Member follower = memberRepository.findById(followerEmail)
                .orElseThrow(() -> new RuntimeException("팔로우 하는 회원 없음"));
        Member following = memberRepository.findById(followingEmail)
                .orElseThrow(() -> new RuntimeException("팔로우 받는 회원 없음"));

        followRepository.deleteByFollowerAndFollowing(follower, following);
    }

    public boolean isFollowing(String followerEmail, String followingEmail) {
        Member follower = memberRepository.findById(followerEmail)
            .orElseThrow(() -> new RuntimeException("회원 없음"));
        Member following = memberRepository.findById(followingEmail)
            .orElseThrow(() -> new RuntimeException("회원 없음"));

        return followRepository.existsByFollowerAndFollowing(follower, following);
    }
}
