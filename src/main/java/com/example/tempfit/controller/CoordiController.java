// src/main/java/com/example/tempfit/controller/CoordiController.java
package com.example.tempfit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.tempfit.service.CoordiService;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/coordi")
@RequiredArgsConstructor
public class CoordiController {

    private final CoordiService coordiService;

    // 메인화면 가이드라인(랜덤/이미지)
    @GetMapping("/guide")
    public ResponseEntity<Map<String, Map<String, Map<String, String>>>> getRandomGuide(
            @RequestParam("temp") int temp) {

        int actualTemp = temp;

        Map<String, Map<String, Map<String, String>>> result = new LinkedHashMap<>();

        // 남성
        Map<String, Map<String, String>> maleRaw =
                coordiService.getRandomClothingWithImage("male", actualTemp);
        LinkedHashMap<String, Map<String, String>> maleMap = new LinkedHashMap<>(maleRaw);
        result.put("male", maleMap);

        // 여성
        Map<String, Map<String, String>> femaleRaw =
                coordiService.getRandomClothingWithImage("female", actualTemp);
        LinkedHashMap<String, Map<String, String>> femaleMap = new LinkedHashMap<>(femaleRaw);
        result.put("female", femaleMap);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(result);
    }
}
