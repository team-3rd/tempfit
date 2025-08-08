package com.example.tempfit.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.tempfit.dto.OpenAIStylistRecommendRes;
import com.example.tempfit.dto.OpenAIStylistRecommendationResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenAIStylistRecommendationService {
    private final OpenAIStylistService openAIService;
    private final OpenAIStylistNaverShoppingService naverShoppingService;

    public List<OpenAIStylistRecommendationResult> recommend(String userPrompt) {
        // 1) OpenAI에서 제품 목록(JSON) 수신
        OpenAIStylistRecommendRes productsResponse = openAIService.getProducts(userPrompt);

        // 2) 각 product.keyword로 네이버 쇼핑 검색
        return productsResponse.getProducts().stream()
                .map(p -> OpenAIStylistRecommendationResult.builder()
                        .product(p)
                        .items(naverShoppingService.search(URLEncoder.encode(p.getKeyword(), StandardCharsets.UTF_8)))
                        .build())
                .toList();
    }
}
