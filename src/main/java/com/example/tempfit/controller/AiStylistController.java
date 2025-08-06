package com.example.tempfit.controller;

import com.example.tempfit.dto.AiStylistDTO;
import com.example.tempfit.service.AiStylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/aistylist")
@RequiredArgsConstructor
public class AiStylistController {

    private final AiStylistService aiStylistService;

    /**
     * AI 코디 가이드 (메인 가이드와 동일 구조)
     * 호출 예: GET /api/aistylist/guide?temp=27
     * 반환 구조:
     * {
     *   "male":   { "top": {...}, "outer": {...}, "bottom": {...}, "shoes": {...} },
     *   "female": { "top": {...}, "outer": {...}, "bottom": {...}, "shoes": {...} }
     * }
     */
    @GetMapping("/guide")
    public ResponseEntity<LinkedHashMap<String, LinkedHashMap<String, AiStylistDTO>>> getAiGuide(
            @RequestParam("temp") int temp) {

        // 남성 리스트 → category 순서 유지하는 LinkedHashMap
        List<AiStylistDTO> maleList   = aiStylistService.recommendOutfit(temp, "male");
        LinkedHashMap<String, AiStylistDTO> maleMap = maleList.stream()
            .collect(Collectors.toMap(
                dto -> dto.getCategory().toLowerCase(),
                dto -> dto,
                (a,b) -> a,
                LinkedHashMap::new
            ));

        // 여성 리스트 → category 순서 유지
        List<AiStylistDTO> femaleList = aiStylistService.recommendOutfit(temp, "female");
        LinkedHashMap<String, AiStylistDTO> femaleMap = femaleList.stream()
            .collect(Collectors.toMap(
                dto -> dto.getCategory().toLowerCase(),
                dto -> dto,
                (a,b) -> a,
                LinkedHashMap::new
            ));

        // 최종 결과: male 먼저, female 다음
        LinkedHashMap<String, LinkedHashMap<String, AiStylistDTO>> result = new LinkedHashMap<>();
        result.put("male",   maleMap);
        result.put("female", femaleMap);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(result);
    }
}
