package com.example.tempfit.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.tempfit.dto.OpenAIStylistRecommendRequest;
import com.example.tempfit.dto.OpenAIStylistRecommendationResult;
import com.example.tempfit.service.OpenAIStylistRecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/aiguide")
public class OpenAIStylistController {

    private final OpenAIStylistRecommendationService recommendationService;

    // GET: /api/aiguide?temp=27  -> { "male":[...], "female":[...] }  (male이 항상 먼저)
    @GetMapping
    public Map<String, List<OpenAIStylistRecommendationResult>> recommendBoth(
            @RequestParam("temp") int temp) {
        return recommendationService.recommendBoth(temp);
    }

    // (유지) POST: 프롬프트 직접 전달 시
    @PostMapping
    public List<OpenAIStylistRecommendationResult> recommend(@RequestBody OpenAIStylistRecommendRequest req) {
        return recommendationService.recommend(req.getPrompt());
    }
}
