package com.example.tempfit.controller;

import com.example.tempfit.dto.CommunityDTO;
import com.example.tempfit.dto.CoordsDTO;
import com.example.tempfit.dto.GridDTO;
import com.example.tempfit.dto.SelectedWeatherDTO;
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
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final CommentService commentService;
    private final MemberRepository memberRepository;
    private final WeatherService weatherService;
    private final SelectedWeatherService selectedWeatherService;

    @GetMapping("/list")
    public String list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "type", defaultValue = "") String type,
            @AuthenticationPrincipal AuthMemberDTO authMemberDTO,
            Model model) {

        Page<CommunityDTO> pageData = keyword == "" && type == "" ? communityService.getPage(page)
                : communityService.searchPage(type, keyword, page);

        // 검색된 유저명
        String user = "";
        try {
            user = keyword != "" && type != "" ? pageData.getContent().get(0).getAuthor().getNickname() : "";
        } catch (Exception e) {
            user = "";
        }

        // 사용자별(좋아요/북마크 등) 상태 적용
        Member me = null;
        if (authMemberDTO != null) {
            me = memberRepository
                    .findByEmailAndFromSocial(authMemberDTO.getEmail(), authMemberDTO.isFromSocial());
        }
        communityService.applyUserFlags(pageData.getContent(), me);

        // 댓글 수 채우기 (간단 구현: N번 조회)
        List<Long> ids = pageData.getContent().stream()
                .map(CommunityDTO::getId)
                .collect(Collectors.toList());
        Map<Long, Integer> countMap = commentService.getCommentCounts(ids);
        pageData.getContent().forEach(dto -> dto.setCommentCount(countMap.getOrDefault(dto.getId(), 0)));

        // 페이징 계산
        int currentPage;
        int totalPages = pageData.getTotalPages();
        int pageBlock = 10;
        int startPage;
        int endPage;

        if (totalPages == 0) {
            currentPage = 0;
            startPage = 0;
            endPage = 0;
        } else {
            currentPage = page;
            startPage = ((currentPage - 1) / pageBlock) * pageBlock + 1;
            endPage = Math.min(startPage + pageBlock - 1, totalPages);
        }

        // 전송된 타입값
        String searchType = type;

        model.addAttribute("list", pageData.getContent());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("type", searchType);
        model.addAttribute("username", user);

        return "community/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthMemberDTO authMemberDTO,
            Model model) {

        CommunityDTO postDto = communityService.get(id);

        // 모달 초기 상태 (좋아요/북마크) 반영
        if (authMemberDTO != null) {
            Member me = memberRepository
                    .findByEmailAndFromSocial(authMemberDTO.getEmail(), authMemberDTO.isFromSocial());
            communityService.applyUserFlags(List.of(postDto), me);
        }

        // 모달도 리스트와 동일하게 DTO에 댓글 수 세팅
        postDto.setCommentCount(commentService.getCommentCount(id));

        model.addAttribute("post", postDto);
        model.addAttribute("comments", commentService.getComments(id));
        return "community/detail :: detailCard";
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
            @RequestParam("imageFiles") List<MultipartFile> imageFiles,
            @RequestParam("repImageIndex") int repImageIndex,
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
        CoordsDTO coords = new CoordsDTO(dto.getLon(), dto.getLat());
        GridDTO grid = weatherService.changeCoords(coords);
        List<SelectedWeatherDTO> weatherData = selectedWeatherService.getWeatherApi(grid, dates);

        Member loginMember = memberRepository.findByEmailAndFromSocial(
                authMemberDTO.getEmail(), authMemberDTO.isFromSocial());

        communityService.register(dto, loginMember, imageFiles, repImageIndex, weatherData);
        return "redirect:/community/list";
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
        Member member = memberRepository.findByEmail(
                authMemberDTO.getEmail()).get();
        communityService.recommendPost(id, member);
        return "redirect:/community/detail/" + id;
    }

    @PostMapping("/bookmark/{id}")
    public String bookmarkPost(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthMemberDTO authMemberDTO) {
        Member member = memberRepository.findByEmail(
                authMemberDTO.getEmail()).get();
        communityService.bookmarkPost(id, member);
        return "redirect:/community/detail/" + id;
    }

    @GetMapping("/detail/{id}/fragment")
    public String detailFragment(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthMemberDTO authMemberDTO,
            Model model) {
        // 기존 detail() 로직 재사용(중복 없음)
        return detail(id, authMemberDTO, model);
    }
}
