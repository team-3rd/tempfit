package com.example.tempfit.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.example.tempfit.service.FollowService;
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
    private final FollowService followService;

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

    @GetMapping("/{email}")
    public String viewProfile(@PathVariable String email,
                           @AuthenticationPrincipal AuthMemberDTO authMember,
                           Model model) {

    Member profileMember = memberRepository.findByEmail(email).get();

    boolean isFollowing = followService.isFollowing(authMember.getEmail(), email);

    model.addAttribute("profile", profileMember);
    model.addAttribute("isFollowing", isFollowing);

    return "member/profile";
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
    @PostMapping("/{email}/profile-image")
    public ResponseEntity<String> updateProfileImage(
            @PathVariable String email,
            @RequestParam("file") MultipartFile file) {

        try {
            memberService.updateProfileImage(email, file);
            return ResponseEntity.ok("프로필 이미지가 변경되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("이미지 업로드 실패");
        }
    }
    
    @DeleteMapping("/{email}/profile-image")
    public ResponseEntity<String> deleteProfileImage(@PathVariable String email) throws IOException {
        memberService.resetToDefaultProfileImage(email);
        return ResponseEntity.ok("프로필 이미지가 기본 이미지로 변경되었습니다.");
    }
}
