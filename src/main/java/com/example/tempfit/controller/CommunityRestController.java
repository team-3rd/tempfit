package com.example.tempfit.controller;

import com.example.tempfit.dto.CommentDTO;
import com.example.tempfit.entity.Comment;
import com.example.tempfit.entity.Member;
import com.example.tempfit.repository.MemberRepository;
import com.example.tempfit.security.AuthMemberDTO;
import com.example.tempfit.service.CommentService;
import com.example.tempfit.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityRestController {
    private final CommunityService communityService;
    private final MemberRepository memberRepository;
    private final CommentService commentService;

    @PostMapping("/view/{id}")
    public ResponseEntity<Void> increaseViewCount(@PathVariable Long id) {
        communityService.increaseViewCount(id);
        return ResponseEntity.ok().build();
    }

    @ResponseBody
    @PostMapping("/detail/{id}/comments")
    public CommentDTO addComment(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthMemberDTO authMemberDTO,
            @RequestParam String content) {

        Member member = memberRepository.findByEmailAndFromSocial(
                authMemberDTO.getEmail(), authMemberDTO.isFromSocial()); // ✅ getEmail로 수정

        CommentDTO commentdto = commentService.addComment(id, member, content);

        return commentdto;
    }
}
