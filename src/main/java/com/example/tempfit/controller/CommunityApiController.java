// src/main/java/com/example/tempfit/controller/CommunityApiController.java
package com.example.tempfit.controller;

import com.example.tempfit.dto.CommunityDTO;
import com.example.tempfit.entity.Member;                    // ★ 추가
import com.example.tempfit.repository.MemberRepository;     // ★ 추가
import com.example.tempfit.security.AuthMemberDTO;          // ★ 추가
import com.example.tempfit.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // ★ 추가
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityApiController {

    private final CommunityService communityService;
    private final MemberRepository memberRepository;        // ★ 추가

    @GetMapping("/best")
    public List<Map<String, Object>> bestLooks(
            @RequestParam int temp,
            @AuthenticationPrincipal AuthMemberDTO auth // ★ 추가: 로그인 사용자 받기
    ) {
        List<CommunityDTO> top = communityService.getTopPostsByTemp(temp, 4);

        // ★ 로그인되어 있다면 각 DTO에 likedByMe/bookmarkedByMe 세팅
        if (auth != null) {
            Member me = memberRepository.findByEmailAndFromSocial(auth.getEmail(), auth.isFromSocial());
            communityService.applyUserFlags(top, me);
        }

        return top.stream().map(dto -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", dto.getId());
            m.put("title", dto.getTitle());
            // authorNickname을 프론트에서 쓰므로 그대로 유지
            m.put("author", dto.getAuthor() != null ? dto.getAuthor().getNickname() : null);
            m.put("authorNickname", dto.getAuthor() != null ? dto.getAuthor().getNickname() : null);
            m.put("profileImageUrl", dto.getProfileImageUrl());
            m.put("content", dto.getContent());
            m.put("recommendCount", dto.getRecommendCount());
            m.put("commentCount", dto.getCommentCount());  // 필요시 0일 수 있음
            m.put("repImageUrl", dto.getRepImageUrl());
            m.put("extraImageUrls", dto.getExtraImageUrls());
            m.put("createdDate", dto.getCreatedDate());
            m.put("minTemp", dto.getMinTemp());
            m.put("maxTemp", dto.getMaxTemp());
            m.put("avgTemp", dto.getAvgTemp());
            m.put("sky", dto.getSky());
            // ★ 프론트가 채움 아이콘을 그릴 수 있도록 상태 포함
            m.put("likedByMe", dto.isLikedByMe());
            m.put("bookmarkedByMe", dto.isBookmarkedByMe());
            return m;
        }).collect(Collectors.toList());
    }
}
