package com.example.tempfit.service;

import com.example.tempfit.dto.AiStylistDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AiStylistService {

    private final RestTemplate openAiRestTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public AiStylistService(RestTemplate openAiRestTemplate) {
        this.openAiRestTemplate = openAiRestTemplate;
    }

    public List<AiStylistDTO> recommendOutfit(int temp, String gender) {
        // 1) 메시지 리스트 준비
        List<Map<String, String>> messages = List.of(
            Map.of(
                "role", "system",
                "content", "너는 전문 스타일리스트야. 무신사나 주요 쇼핑몰을 참고해서 코디를 추천해."
            ),
            Map.of(
                "role", "user",
                "content", String.format(
                    "현재 기온은 %d도이고, 성별은 %s입니다. " +
                    "상의 → 아우터 → 하의 → 신발 순으로 JSON 배열 형태로 추천해줘. " +
                    "필드는 category, brandName, productName, imageUrl, linkUrl 입니다. " +
                    "반드시 JSON 배열만, 부가 텍스트나 설명 없이 순수 JSON으로만 응답해주세요.",
                    temp, gender
                )
            )
        );

        // 2) 요청 바디 구성
        Map<String, Object> body = Map.of(
            "model", "gpt-4o-mini",
            "messages", messages,
            "max_tokens", 512
        );

        // 3) 호출 (RestTemplate 인터셉터가 Authorization 헤더를 붙여 줍니다)
        String resp = openAiRestTemplate.postForObject(
            OPENAI_URL,
            body,
            String.class
        );

        try {
            // 4) OpenAI 응답에서 choices[0].message.content 추출
            JsonNode root = objectMapper.readTree(resp);
            String content = root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();

            // 5) 코드 펜스(```)나 백틱 제거
            String json = content.trim()
                // 코드 블록 펜스 라인 전체 제거
                .replaceAll("(?m)^```.*$", "")
                // 남아 있는 모든 백틱 제거
                .replace("`", "")
                .trim();

            // 6) JSON 배열 → List<AiStylistDTO> 로 변환
            return objectMapper.readValue(
                json,
                new TypeReference<List<AiStylistDTO>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("AI 응답 파싱 또는 호출 실패: " + e.getMessage(), e);
        }
    }
}
