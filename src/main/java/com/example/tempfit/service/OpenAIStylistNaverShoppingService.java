package com.example.tempfit.service;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.tempfit.dto.OpenAIStylistNaverShoppingItem;

@Service
public class OpenAIStylistNaverShoppingService {
        @Value("${naver.client-id}")
        private String clientId;

        @Value("${naver.client-secret}")
        private String clientSecret;

        @Value("${naver.base-url}")
        private String baseUrl;

        @Value("${naver.display}")
        private Integer display;

        public List<OpenAIStylistNaverShoppingItem> search(String keyword) {
                WebClient client = WebClient.builder()
                                .baseUrl(baseUrl)
                                .defaultHeader("X-Naver-Client-Id", clientId)
                                .defaultHeader("X-Naver-Client-Secret", clientSecret)
                                .build();

                URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                                .queryParam("query", keyword)
                                .queryParam("display", display)
                                .build(true) // 인코딩 그대로
                                .toUri();

                Map resp = client.get()
                                .uri(uri)
                                .retrieve()
                                .bodyToMono(Map.class)
                                .block();

                // resp.get("items")를 파싱
                List<Map<String, Object>> items = (List<Map<String, Object>>) resp.get("items");
                if (items == null)
                        return List.of();

                return items.stream().map(m -> OpenAIStylistNaverShoppingItem.builder()
                                .title((String) m.get("title"))
                                .link((String) m.get("link"))
                                .image((String) m.get("image"))
                                .lprice((String) m.get("lprice"))
                                .mallName((String) m.get("mallName"))
                                .build()).collect(Collectors.toList());
        }

}
