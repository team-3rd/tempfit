package com.example.tempfit.controller;

import com.example.tempfit.dto.CommunityDTO;
import com.example.tempfit.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityApiController {
    private final CommunityService communityService;

    @GetMapping("/best")
    public Map<String, Map<String, Object>> bestLooks(@RequestParam int temp) {
        // 서비스에서 스타일별 리스트(최대 1개)를 받아온다
        Map<String, List<CommunityDTO>> raw = communityService.getPostsByTempAndStyle(temp, 1);

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        raw.forEach((style, list) -> {
            if (!list.isEmpty()) {
                CommunityDTO dto = list.get(0);
                Map<String, Object> m = new HashMap<>();
                m.put("id", dto.getId());
                m.put("title", dto.getTitle());
                m.put("repImageUrl", dto.getRepImageUrl());
                m.put("recommendCount", dto.getRecommendCount());
                m.put("authorName", dto.getAuthor().getName());
                m.put("casual", dto.isCasual());
                m.put("street", dto.isStreet());
                m.put("formal", dto.isFormal());
                m.put("outdoor", dto.isOutdoor());
                result.put(style, m);
            }
        });

        return result;
    }
}
