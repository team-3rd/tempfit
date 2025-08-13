package com.example.tempfit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.tempfit.service.ClothingGuideService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coordi")
@RequiredArgsConstructor
public class CoordiController {

    private final ClothingGuideService clothingGuideService;

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
