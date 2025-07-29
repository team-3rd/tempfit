package com.example.tempfit.controller;

import com.example.tempfit.dto.CommunityDTO;
import com.example.tempfit.dto.CoordsDTO;
import com.example.tempfit.dto.GridDTO;
import com.example.tempfit.dto.SelectedWeatherDTO;
import com.example.tempfit.entity.Board; // ← import 추가
import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Sex;
import com.example.tempfit.repository.MemberRepository;
import com.example.tempfit.security.AuthMemberDTO;
import com.example.tempfit.service.CommentService;
import com.example.tempfit.service.CommunityService;
import com.example.tempfit.service.SelectedWeatherService;
import com.example.tempfit.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final CommentService commentService;
    private final MemberRepository memberRepository;
    private final WeatherService weatherService;
    private final SelectedWeatherService selectedWeatherService;

    /**
     * 게시판별 리스트 조회
     */
    @GetMapping("/list")
    public String list(
            @RequestParam(value = "board", defaultValue = "FREE") Board board, // ← 추가
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {
        Page<CommunityDTO> pageData = communityService.getPageByBoard(board, page);

        int currentPage = page;
        int totalPages = pageData.getTotalPages();
        int pageBlock = 10;
        int startPage = ((currentPage - 1) / pageBlock) * pageBlock + 1;
        int endPage = Math.min(startPage + pageBlock - 1, totalPages);

        model.addAttribute("list", pageData.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("board", board); // 뷰에서 분기 렌더링에 사용

        return "community/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        CommunityDTO postDto = communityService.get(id);
        model.addAttribute("post", postDto);
        model.addAttribute("comments", commentService.getComments(id));
        return "community/detail";
    }

    @PostMapping("/detail/{id}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam(required = false) Member author,
            @RequestParam String content) {
        commentService.addComment(id, author, content);
        return "redirect:/community/detail/" + id;
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("communityDTO", new CommunityDTO());
        return "community/create";
    }

    @PostMapping("/register")
    public String registerPost(
            @ModelAttribute("communityDTO") CommunityDTO dto,
            @RequestParam(value = "styleNames", required = false) List<String> styleNames,
            @RequestParam("repImage") MultipartFile repImage,
            @RequestParam(value = "extraImages", required = false) List<MultipartFile> extraImages,
            @AuthenticationPrincipal AuthMemberDTO authMemberDTO,
            @RequestParam(value = "sexSet", required = false) List<Sex> sexSet) throws IOException {
        if (sexSet != null) {
            dto.setSexSet(sexSet);
            dto.setMale(sexSet.contains(Sex.MALE));
            dto.setFemale(sexSet.contains(Sex.FEMALE));
        }
        if (styleNames != null) {
            dto.setStyleNames(styleNames);
            dto.setCasual(styleNames.contains("CASUAL"));
            dto.setStreet(styleNames.contains("STREET"));
            dto.setFormal(styleNames.contains("FORMAL"));
            dto.setOutdoor(styleNames.contains("OUTDOOR"));
        }

        LocalDate dates = dto.getDates();
        CoordsDTO coords = new CoordsDTO(dto.getLat(), dto.getLon());
        GridDTO grid = weatherService.changeCoords(coords);
        List<SelectedWeatherDTO> weatherData = selectedWeatherService.getWeatherApi(grid, dates);

        Member loginMember = memberRepository.findByEmailAndFromSocial(
                authMemberDTO.getEmail(), authMemberDTO.isFromSocial());

        Long newId = communityService.register(dto, loginMember, repImage, extraImages, weatherData);
        return "redirect:/community/detail/" + newId;
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
        if (styleNames != null) {
            dto.setStyleNames(styleNames);
            dto.setCasual(styleNames.contains("CASUAL"));
            dto.setStreet(styleNames.contains("STREET"));
            dto.setFormal(styleNames.contains("FORMAL"));
            dto.setOutdoor(styleNames.contains("OUTDOOR"));
        }

        LocalDate dates = dto.getDates();
        CoordsDTO coords = new CoordsDTO(dto.getLat(), dto.getLon());
        GridDTO grid = weatherService.changeCoords(coords);
        List<SelectedWeatherDTO> weatherData = selectedWeatherService.getWeatherApi(grid, dates);

        Member loginMember = memberRepository.findByEmailAndFromSocial(
                authMemberDTO.getUsername(), authMemberDTO.isFromSocial());
        communityService.modify(dto, loginMember, repImage, extraImages, removeRepImage, weatherData);
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
