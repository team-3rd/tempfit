package com.example.tempfit.service;

import java.util.Optional;

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

    public void follow(String targetEmail, String currentEmail) {
        if(targetEmail.equals(currentEmail))
            new RuntimeException("스스로 팔로우 할 수 없습니다.");

        Member follower = memberRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 회원이 없습니다."));
        Member following = memberRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new IllegalArgumentException("대상 회원이 없습니다."));

                Optional<Follow> exitFol = followRepository.findByFollowerAndFollowing(follower, following);
        if (exitFol.isPresent()) {
            followRepository.delete(exitFol.get());
        }
        else {
            Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

            followRepository.save(follow);
        }
    }

    public boolean isFollowing(String followerEmail, String targetEmail) {
    Member follower = memberRepository.findByEmail(followerEmail).orElse(null);
    Member target = memberRepository.findByEmail(targetEmail).orElse(null);
    if (follower == null || target == null) return false;

    return followRepository.existsByFollowerAndFollowing(follower, target);
    }


    public long countFollowers(Member member) {
        return followRepository.countByFollowing(member);
    }

    public long countFollowing(Member member) {
        return followRepository.countByFollower(member);
    }
}
