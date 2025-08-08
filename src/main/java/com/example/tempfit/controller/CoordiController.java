package com.example.tempfit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.tempfit.service.ClothingGuideService;
import com.example.tempfit.service.CoordiService;
import com.example.tempfit.dto.CoordiDTO;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coordi")
@RequiredArgsConstructor
public class CoordiController {

    private final CoordiService coordiService;
    private final ClothingGuideService clothingGuideService;

    // 게시글 등록
//     @PostMapping
//     public Long register(@RequestBody CoordiDTO dto) {
//         return coordiService.register(dto);
//     }

    // 온도별 스타일별 TOP3 추천 게시글
    @GetMapping("/recommend")
    public ResponseEntity<Map<String, List<CoordiDTO>>> getRecommendations(@RequestParam("temp") int temp) {
        Map<String, List<CoordiDTO>> recommendations = coordiService.getRecommendationsByTemp(temp);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(recommendations);
    }

    // 온도범위별 전체 게시글 리스트 (추천순)
    @GetMapping("/list")
    public ResponseEntity<List<CoordiDTO>> getAllByTemperature(@RequestParam("temp") int temp) {
        List<CoordiDTO> list = coordiService.getAllByTemperature(temp);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(list);
    }

    // 메인화면 가이드라인(랜덤/이미지)
    @GetMapping("/guide")
    public ResponseEntity<Map<String, Map<String, Map<String, String>>>> getRandomGuide(
            @RequestParam("temp") int temp) {
        // temp는 반드시 전달되므로, null 체크 불필요
        int actualTemp = temp;

        Map<String, Map<String, Map<String, String>>> result = new LinkedHashMap<>();

        // 남성
        LinkedHashMap<String, Map<String, String>> maleMap = clothingGuideService
                .getRandomMaleClothingByTemperature(actualTemp)
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Map.of(
                                "name", e.getValue().getClothName(),
                                "imageUrl", e.getValue().getImageUrl()),
                        (a, b) -> a,
                        LinkedHashMap::new));
        result.put("male", maleMap);

        // 여성
        LinkedHashMap<String, Map<String, String>> femaleMap = clothingGuideService
                .getRandomFemaleClothingByTemperature(actualTemp)
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Map.of(
                                "name", e.getValue().getClothName(),
                                "imageUrl", e.getValue().getImageUrl()),
                        (a, b) -> a,
                        LinkedHashMap::new));
        result.put("female", femaleMap);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(result);
    }

}
