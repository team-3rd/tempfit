package com.example.tempfit.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tempfit.security.AuthMemberDTO;
import com.example.tempfit.service.FollowService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follow")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{email}")
    public ResponseEntity<?> follow(@AuthenticationPrincipal AuthMemberDTO user,
                                    @PathVariable String email) {
        followService.follow(user.getUsername(), email);
        return ResponseEntity.ok("팔로우 성공");
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<?> unfollow(@AuthenticationPrincipal AuthMemberDTO user,
                                      @PathVariable String email) {
        followService.unfollow(user.getUsername(), email);
        return ResponseEntity.ok("언팔로우 성공");
    }
}
