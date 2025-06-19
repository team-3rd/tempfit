package com.example.tempfit.controller;

import com.example.tempfit.dto.CommunityDTO;
import com.example.tempfit.service.CommunityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Slf4j
public class CommunityController {

    private final CommunityService communityService;

    /**
     * 게시글 등록 (Multipart/Form-Data)
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Long registerPost(@ModelAttribute CommunityDTO dto) throws IOException {
        log.info("등록 요청: {}", dto);
        return communityService.register(
            dto,
            dto.getRepImage(),
            dto.getExtraImages()
        );
    }

    /**
     * 페이징된 리스트 조회 (전체)
     */
    @GetMapping("/list")
    public Page<CommunityDTO> list(
        @PageableDefault(size = 25, sort = "id", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
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
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void modify(
        @PathVariable Long id,
        @ModelAttribute CommunityDTO dto
    ) throws IOException {
        dto.setId(id);
        log.info("수정 요청: {}", dto);
        communityService.modify(
            dto,
            dto.getRepImage(),
            dto.getExtraImages()
        );
    }

    /**
     * 게시글 삭제
     */
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
        @RequestParam(value = "page", defaultValue = "1") int page
    ) {
        return communityService.searchPageRaw(type, keyword, styleNames, page);
    }
}
