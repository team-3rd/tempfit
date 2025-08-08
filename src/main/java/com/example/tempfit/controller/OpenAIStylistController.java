package com.example.tempfit.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping
    public List<OpenAIStylistRecommendationResult> recommend(@RequestBody OpenAIStylistRecommendRequest req) {
        return recommendationService.recommend(req.getPrompt());
    }

    
}
