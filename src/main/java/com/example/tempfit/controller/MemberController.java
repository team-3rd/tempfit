package com.example.tempfit.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
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
import com.example.tempfit.dto.ProductsDTO;
import com.example.tempfit.entity.Member;
import com.example.tempfit.repository.BookmarkRepository;
import com.example.tempfit.repository.MemberRepository;
import com.example.tempfit.security.AuthMemberDTO;
import com.example.tempfit.service.CommunityService;
import com.example.tempfit.service.FileStorageService;
import com.example.tempfit.service.FollowService;
import com.example.tempfit.service.MemberService;
import com.example.tempfit.service.ProductsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;
    private final MemberService memberService;
    private final CommunityService communityService;
    private final FollowService followService;
    private final ProductsService productsService;

    // @PreAuthorize("permitAll()")
    @GetMapping("/login")
    public void getLogin() {
        log.info("login 폼 요청");
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/register")
    public void getRegister(@ModelAttribute("dto") MemberDTO dto) {
        log.info("회원가입 폼 요청");
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/register")
    public String postRegister(@ModelAttribute("dto") @Valid MemberDTO dto) {
        log.info("회원가입 요청 {}", dto);
        memberService.register(dto);
        return "redirect:/member/login";
    }

    @GetMapping("/mypage")
    public void getMypage(Model model, Authentication authentication) {
        log.info("Mypage 요청");
        String email = authentication.getName();
        MemberDTO memberDTO = memberService.getMember(email);
        model.addAttribute("dto", memberDTO);
    }

    @GetMapping("/{email}")
    public String getProfile(@PathVariable String email, @AuthenticationPrincipal AuthMemberDTO authMemberDTO,
            Model model, CsrfToken csrfToken) {

        Member profileMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 회원이 존재하지 않습니다."));
        String myEmail = null;
        if (authMemberDTO != null)
            myEmail = authMemberDTO.getEmail();

        boolean isOwner = authMemberDTO != null && myEmail.equals(email);

        boolean isFollowing = false;
        if (!isOwner && authMemberDTO != null) {
            isFollowing = followService.isFollowing(myEmail, email);
        }
        long followerCount = followService.countFollowers(profileMember);
        long followingCount = followService.countFollowing(profileMember);

        List<CommunityDTO> posts = communityService.getPostsByMember(profileMember);
        communityService.applyUserFlags(posts, profileMember);
        List<CommunityDTO> bookmarks = communityService
                .getPostsByIds(bookmarkRepository.findCommunityIdsByMemberEmail(email));
        List<ProductsDTO> products = memberService.getDibList(email);

        model.addAttribute("profile", profileMember);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("followerCount", followerCount);
        model.addAttribute("followingCount", followingCount);
        model.addAttribute("posts", posts);
        model.addAttribute("bookmarks", bookmarks);
        model.addAttribute("products", products);
        model.addAttribute("_csrf", csrfToken);

        return "member/profile";
    }

    @GetMapping("/mychange")
    public void getMychange(Authentication authentication, Model model) {
        log.info("Mychange 요청");
        String email = authentication.getName();
        MemberDTO dto = memberService.getMember(email);
        model.addAttribute("dto", dto);
    }

    @PostMapping("/mychange")
    public String postMychange(@ModelAttribute("dto") @Valid MemberDTO dto, Authentication authentication) {
        log.info("Mychange 요청");
        String email = authentication.getName();
        if (memberService.checkPw(dto.getEmail(), dto.getPassword())) {
            memberService.update(email, dto);
            return "redirect:/member/" + dto.getEmail();
        }
        return "redirect:/member/" + dto.getEmail();
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
