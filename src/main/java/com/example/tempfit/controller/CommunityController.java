package com.example.tempfit.controller;

import com.example.tempfit.dto.CommunityDTO;
import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.Member;
import com.example.tempfit.security.LoginMemberDetails;
import com.example.tempfit.service.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Slf4j
public class CommunityController {

    private final CommunityService communityService;

    /**
     * 게시글 등록 (Multipart/Form-Data)
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Long registerPost(@ModelAttribute CommunityDTO dto, @AuthenticationPrincipal LoginMemberDetails loginMemberDetails) throws IOException {
        log.info("등록 요청: {}", dto);
        Member loginMember = loginMemberDetails.getMember();
        return communityService.register(
            dto,
            loginMember,
            dto.getRepImage(),
            dto.getExtraImages()
        );
    }

    /**
     * 페이징된 리스트 조회 (전체)
     */
    @PreAuthorize("permitAll()")
    @GetMapping("/list")
    public Page<CommunityDTO> list(
            @PageableDefault(size = 25, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        // pageable.getPageNumber()는 0-based이므로 +1
        return communityService.getPage(pageable.getPageNumber() + 1);
    }

    /**
     * 단건 조회
     */
    @GetMapping("/{id}")
    public CommunityDTO getOne(@PathVariable Long id) {
        return communityService.get(id);
    }

    /**
     * 게시글 수정 (Multipart/Form-Data)
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void modify(
        @PathVariable Long id,
        @ModelAttribute CommunityDTO dto,
        @AuthenticationPrincipal LoginMemberDetails loginMemberDetails
    ) throws IOException {
        dto.setId(id);
        Member loginMember = loginMemberDetails.getMember();
        log.info("수정 요청: {}", dto);
        communityService.modify(
                dto,
                dto.getRepImage(),
                dto.getExtraImages());
    }

    /**
     * 게시글 삭제
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'MANAGER')")
    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        log.info("삭제 요청: id={}", id);
        communityService.remove(id);
    }

    /**
     * 검색 + 스타일 필터 + 페이징
     */
    @GetMapping("/search")
    public Page<CommunityDTO> searchPage(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "styleNames", required = false) List<String> styleNames,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        return communityService.searchPageRaw(type, keyword, styleNames, page);
    }

    // 메인 홈페이지에 top 코디들 추가
    @GetMapping("/api/community/top")
    @ResponseBody
    public Map<String, List<CommunityDTO>> getTopCommunitiesByStyleAndTemperature(@RequestParam("temp") int temp) {
        Map<String, List<Community>> result = communityService.getTopCommunitiesByStyleAndTemperature(temp);

        // Entity → DTO 변환 (서비스단에서 바로 DTO 반환해도 됨. 지금 구조면 여기서 변환)
        Map<String, List<CommunityDTO>> dtoResult = new HashMap<>();
        result.forEach((style, posts) -> {
            dtoResult.put(style, posts.stream()
                .map(communityService::entityToDTO) // entityToDTO는 public으로 변경
                .collect(Collectors.toList()));
        });
        return dtoResult;
    }
}
