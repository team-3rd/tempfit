package com.example.tempfit.controller;

import com.example.tempfit.dto.CommunityDTO;
import com.example.tempfit.dto.CoordsDTO;
import com.example.tempfit.dto.GridDTO;
import com.example.tempfit.dto.SelectedWeatherDTO;
import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Sex;
import com.example.tempfit.repository.MemberRepository;
import com.example.tempfit.security.AuthMemberDTO;
import com.example.tempfit.security.LoginMemberDetails;
import com.example.tempfit.service.CommentService;
import com.example.tempfit.service.CommunityService;
import com.example.tempfit.service.SelectedWeatherService;
import com.example.tempfit.service.WeatherService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final CommentService commentService;
    private final MemberRepository memberRepository;
    private final WeatherService weatherService;
    private final SelectedWeatherService selectedWeatherService;

    // 게시글 목록
    @GetMapping("/list")
    public String list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "styleNames", required = false) List<String> styleNames,
            @RequestParam(value = "sexs", required = false) List<String> sexs,
            Model model) {
        var result = communityService.searchPageRaw(type, keyword, styleNames, page);
        int currentPage = page;
        int totalPages = result.getTotalPages();
        int pageBlock = 10;
        int startPage = ((currentPage - 1) / pageBlock) * pageBlock + 1;
        int endPage = Math.max(1, Math.min(startPage + pageBlock - 1, totalPages));

        model.addAttribute("list", result.getContent());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("styleNames", styleNames);
        model.addAttribute("sexs", sexs);

        return "community/list";
    }

    // 상세 페이지
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var postDto = communityService.get(id);
        var comments = commentService.getComments(id);
        model.addAttribute("post", postDto);
        model.addAttribute("comments", comments);
        return "community/detail";
    }

    // 댓글 등록 처리 (익명)
    @PostMapping("/detail/{id}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam(required = false) String authorName,
            @RequestParam String content) {
        commentService.addComment(id, authorName, content);
        return "redirect:/community/detail/" + id;
    }

    // 글쓰기 폼
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("communityDTO", new CommunityDTO());
        return "community/create";
    }

    // 글 등록 처리 (대표 이미지 + 추가 이미지)
    @PostMapping("/register")
    public String registerPost(
            @ModelAttribute("communityDTO") CommunityDTO dto,
            @RequestParam(value = "styleNames", required = false) List<String> styleNames,
            @RequestParam(value = "times", required = false) List<String> times,
            @RequestParam("repImage") MultipartFile repImage,
            @RequestParam(value = "extraImages", required = false) List<MultipartFile> extraImages,
            @AuthenticationPrincipal AuthMemberDTO authMemberDTO,
            @RequestParam(value = "sexSet", required = false) List<Sex> sexSet) throws IOException {

        if (sexSet != null) {
            dto.setSexSet(sexSet);
            dto.setMale(sexSet.contains(Sex.MALE));
            dto.setFemale(sexSet.contains(Sex.FEMALE));
        }
        // 스타일 처리
        if (styleNames != null) {
            dto.setStyleNames(styleNames);
            dto.setCasual(styleNames.contains("CASUAL"));
            dto.setStreet(styleNames.contains("STREET"));
            dto.setFormal(styleNames.contains("FORMAL"));
            dto.setOutdoor(styleNames.contains("OUTDOOR")); // <-- 변경된 부분
        }

        if (times != null) {
            dto.setTimes(times);
            dto.setDayTime(times.contains("dayTimes"));
            dto.setNightTime(times.contains("nightTimes"));
        }

        LocalDate dates = dto.getDates();
        CoordsDTO coords = new CoordsDTO();
        coords.setLat(dto.getLat());
        coords.setLon(dto.getLon());
        GridDTO grid = weatherService.changeCoords(coords);

        List<SelectedWeatherDTO> weatherList = selectedWeatherService.getWeatherApi(grid, dates);

        Member loginMember = memberRepository.findByEmailAndFromSocial(
                authMemberDTO.getUsername(), authMemberDTO.isFromSocial());
        // 서비스 호출: 게시글 + 이미지 저장
        communityService.register(dto, loginMember, repImage, extraImages, weatherList);
        return "redirect:/community/list";
    }

    @GetMapping("/edit/{id}")
    public String editPost(@PathVariable Long id, Model model) {
        CommunityDTO dto = communityService.get(id);
        model.addAttribute("communityDTO", dto);
        return "community/edit";
    }

    @PostMapping("/edit/{id}")
    public String editPost(
            @PathVariable Long id,
            @ModelAttribute("communityDTO") CommunityDTO dto,
            @RequestParam(value = "styleNames", required = false) List<String> styleNames,
            @RequestParam(value = "repImage", required = false) MultipartFile repImage,
            @RequestParam(value = "extraImages", required = false) List<MultipartFile> extraImages,
            @RequestParam(value = "removeRepImage", defaultValue = "false") boolean removeRepImage,
            @AuthenticationPrincipal AuthMemberDTO authMemberDTO,
            @RequestParam(value = "sexSet", required = false) List<Sex> sexSet) throws IOException {
                
        if (sexSet != null) {
            dto.setSexSet(sexSet);
            dto.setMale(sexSet.contains(Sex.MALE));
            dto.setFemale(sexSet.contains(Sex.FEMALE));
        }

        // 스타일 처리
        if (styleNames != null) {
            dto.setStyleNames(styleNames);
            dto.setCasual(styleNames.contains("CASUAL"));
            dto.setStreet(styleNames.contains("STREET"));
            dto.setFormal(styleNames.contains("FORMAL"));
            dto.setOutdoor(styleNames.contains("OUTDOOR"));
        }
    
        Member loginMember = memberRepository.findByEmailAndFromSocial(
                authMemberDTO.getUsername(), authMemberDTO.isFromSocial());
        communityService.modify(dto, loginMember, repImage, extraImages, removeRepImage);
        return "redirect:/community/detail/" + id;
    }
    

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        communityService.remove(id);
        return "redirect:/community/list";
    }

    @PostMapping("/recommend/{id}")
    public String recommendPost(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthMemberDTO authMemberDTO) {
        Member member = memberRepository.findByEmailAndFromSocial(
                authMemberDTO.getUsername(), authMemberDTO.isFromSocial());

        communityService.recommendPost(id, member);
        return "redirect:/community/detail/" + id;
    }
}
