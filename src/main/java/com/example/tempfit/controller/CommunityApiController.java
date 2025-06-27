package com.example.tempfit.controller;

import com.example.tempfit.dto.CommunityDTO;
import com.example.tempfit.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityApiController {
    private final CommunityService communityService;

    /**
     * GET /api/community/top?temp=23
     * 스타일별 추천 게시글 1개씩 반환 (없으면 null)
     * {
     *   "CASUAL": CommunityDTO,
     *   "FORMAL": CommunityDTO,
     *   "STREET": CommunityDTO,
     *   "OUTDOOR": CommunityDTO
     * }
     */
    @GetMapping("/top")
    public Map<String, CommunityDTO> getTopByTemp(@RequestParam("temp") int temp) {
        return communityService.getTopBySelectedTemp(temp);
    }
}
