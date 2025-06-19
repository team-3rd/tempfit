package com.example.tempfit.controller;

import com.example.tempfit.dto.CommunityDTO;
import com.example.tempfit.service.CommentService;
import com.example.tempfit.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityPageController {

    private final CommunityService communityService;
    private final CommentService   commentService;

    // 게시글 목록
    @GetMapping("/list")
    public String list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "styleNames", required = false) List<String> styleNames,
            Model model
    ) {
        var result      = communityService.searchPageRaw(type, keyword, styleNames, page);
        int currentPage = page;
        int totalPages  = result.getTotalPages();
        int pageBlock   = 10;
        int startPage   = ((currentPage - 1) / pageBlock) * pageBlock + 1;
        int endPage     = Math.min(startPage + pageBlock - 1, totalPages);

        model.addAttribute("list",        result.getContent());
        model.addAttribute("totalPages",  totalPages);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("startPage",   startPage);
        model.addAttribute("endPage",     endPage);
        model.addAttribute("type",        type);
        model.addAttribute("keyword",     keyword);
        model.addAttribute("styleNames",  styleNames);

        return "community/list";
    }

    // 상세 페이지
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var postDto  = communityService.get(id);
        var comments = commentService.getComments(id);
        model.addAttribute("post",     postDto);
        model.addAttribute("comments", comments);
        return "community/detail";
    }

    // 댓글 등록 처리 (익명)
    @PostMapping("/detail/{id}/comments")
    public String addComment(
            @PathVariable Long id,
            @RequestParam(required = false) String authorName,
            @RequestParam String content
    ) {
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
            @RequestParam("repImage") MultipartFile repImage,
            @RequestParam(value = "extraImages", required = false) List<MultipartFile> extraImages
    ) throws IOException {
        // 스타일 처리
        if (styleNames != null) {
            dto.setStyleNames(styleNames);
            dto.setCasual   (styleNames.contains("CASUAL"));
            dto.setStreet   (styleNames.contains("STREET"));
            dto.setFormal   (styleNames.contains("FORMAL"));
            dto.setOutdoor  (styleNames.contains("OUTDOOR"));   // <-- 변경된 부분
        }
        // 서비스 호출: 게시글 + 이미지 저장
        communityService.register(dto, repImage, extraImages);
        return "redirect:/community/list";
    }
}
