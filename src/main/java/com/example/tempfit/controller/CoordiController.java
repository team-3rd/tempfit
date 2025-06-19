package com.example.tempfit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.example.tempfit.service.CoordiService;
import com.example.tempfit.dto.CoordiDTO;
import com.example.tempfit.entity.TemperatureRange;
import com.example.tempfit.guide.ClothingGuide;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coordi")
@RequiredArgsConstructor
public class CoordiController {

    private final CoordiService coordiService;

    // 게시글 등록
    @PostMapping
    public Long register(@RequestBody CoordiDTO dto) {
        return coordiService.register(dto);
    }

    // 온도별 스타일별 TOP5 추천 게시글
    @GetMapping("/recommend")
    public Map<String, List<CoordiDTO>> getRecommendations(@RequestParam("temp") int temp) {
        return coordiService.getRecommendationsByTemp(temp);
    }

    // 온도범위별 전체 게시글 리스트 (추천순)
    @GetMapping("/list")
    public List<CoordiDTO> getAllByTemperature(@RequestParam("temp") int temp) {
        return coordiService.getAllByTemperature(temp);
    }

    // 메인화면 가이드라인(랜덤/이미지)
    @GetMapping("/guide")
    public Map<String, Map<String, String>> getRandomGuide(@RequestParam("temp") int temp) {
        TemperatureRange range = TemperatureRange.fromTemperature(temp);
        return ClothingGuide.getRandomClothingWithImage(range);
    }
}
