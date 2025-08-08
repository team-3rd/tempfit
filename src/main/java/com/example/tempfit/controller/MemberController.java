package com.example.tempfit.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.tempfit.dto.CommunityDTO;
import com.example.tempfit.dto.MemberDTO;
import com.example.tempfit.entity.Member;
import com.example.tempfit.repository.MemberRepository;
import com.example.tempfit.security.AuthMemberDTO;
import com.example.tempfit.service.CommunityService;
import com.example.tempfit.service.FileStorageService;
import com.example.tempfit.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final CommunityService communityService;
    private final FileStorageService fileStorageService;

    //@PreAuthorize("permitAll()")
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

    @GetMapping("/mypage")
    public void getMypage(Model model, Authentication authentication){
        log.info("Mypage 요청");
        String email = authentication.getName();
        MemberDTO memberDTO = memberService.getMember(email);
        model.addAttribute("dto", memberDTO);
    }

    @GetMapping("/mychange")
    public void getMychange(Authentication authentication, Model model){
        log.info("Mychange 요청");
        String email = authentication.getName();
        MemberDTO dto = memberService.getMember(email);
        model.addAttribute("dto", dto);
    }

    @PostMapping("/mychange")
    public String postMychange(@ModelAttribute("dto") @Valid MemberDTO dto, Authentication authentication){
        log.info("Mychange 요청");
        String email = authentication.getName();
        if(memberService.checkPw(dto.getEmail(), dto.getPassword()))
        {
            memberService.update(email,dto);
            return "redirect:/member/mypage";
        }
        return "redirect:/member/mypage";
    }

    @GetMapping("/mypage/posts")
    public String myPosts(@AuthenticationPrincipal AuthMemberDTO authMemberDTO, Model model) {
    Member member = memberRepository.findByEmailAndFromSocial(
            authMemberDTO.getEmail(), authMemberDTO.isFromSocial());

    List<CommunityDTO> posts = communityService.getPostsByMember(member);
    model.addAttribute("myPosts", posts);
    return "member/myposts";
}
@PostMapping("/members/{email}/upload-profile")
public ResponseEntity<String> uploadProfileImage(
        @PathVariable String email,
        @RequestParam("file") MultipartFile file) {

    // 1. 파일 저장 (예: 로컬 or S3)
    String imageUrl = fileStorageService.save(file, "profile");
    // 2. 멤버 조회 및 URL 저장
    Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("회원 없음"));

    member.setProfileImageUrl(imageUrl);
    memberRepository.save(member);

    return ResponseEntity.ok(imageUrl);
}
}
