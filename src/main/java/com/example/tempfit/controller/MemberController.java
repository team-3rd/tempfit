package com.example.tempfit.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.tempfit.dto.MemberDTO;
import com.example.tempfit.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PreAuthorize("permitAll()")
    @GetMapping("/login")
    public void getLogin(){
        log.info("login 폼 요청");
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/register")
    public void getRegister(@ModelAttribute("dto") MemberDTO dto){
        log.info("회원가입 폼 요청");
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/register")
    public String postRegister(@ModelAttribute("dto") @Valid MemberDTO dto){
        log.info("회원가입 요청 {}", dto);
        memberService.register(dto);
        return "redirect:/member/login";
    }
}
