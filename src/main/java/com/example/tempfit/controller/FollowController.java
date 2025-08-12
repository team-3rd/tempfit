package com.example.tempfit.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

import com.example.tempfit.security.AuthMemberDTO;
import com.example.tempfit.service.FollowService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/follow")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{email}")
    public String follow(@PathVariable String email,
                                     @AuthenticationPrincipal AuthMemberDTO authMember) {
        followService.follow(email, authMember.getEmail());
        return "redirect:/member/" + email;
    }
}
