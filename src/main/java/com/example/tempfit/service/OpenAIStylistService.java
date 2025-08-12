package com.example.tempfit.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.tempfit.dto.OpenAIStylistRecommendResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Service
public class OpenAIStylistService {

  @Value("${openai.api-key}")
  private String openAiKey;

  @Value("${openai.base-url}")
  private String openAiBaseUrl;

  @Value("${openai.model}")
  private String model;

  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * OpenAI Responses API 호출:
   * products[].{category, brandName, productName} 만 사용 (keyword 제거)
   */
  public OpenAIStylistRecommendResponse getProducts(String userPrompt) {
    WebClient client = WebClient.builder()
        .baseUrl(openAiBaseUrl)
        .defaultHeader("Authorization", "Bearer " + openAiKey)
        .build();

    // ✅ keyword 삭제 (properties에도 없고 required에도 없음)
    Map<String, Object> schema = Map.of(
        "type", "object",
        "properties", Map.of(
            "products", Map.of(
                "type", "array",
                "items", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "category", Map.of("type", "string"),
                        "brandName", Map.of("type", "string"),
                        "productName", Map.of("type", "string")
                    ),
                    "required", List.of("category", "brandName", "productName"),
                    "additionalProperties", false
                )
            )
        ),
        "required", List.of("products"),
        "additionalProperties", false
    );

    Map<String, Object> body = new HashMap<>();
    body.put("model", model);
    body.put("text", Map.of(
        "format", Map.of(
            "type", "json_schema",
            "name", "ProductList",
            "strict", true,
            "schema", schema
        )
    ));
    body.put("input", List.of(
        Map.of("role", "system", "content",
            "You are a helpful fashion stylist. Return ONLY JSON matching the schema."),
        Map.of("role", "user", "content", userPrompt)
    ));

    Map<String, Object> resp = client.post()
        .uri("/responses")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .onStatus(HttpStatusCode::isError, r -> r.bodyToMono(String.class)
            .flatMap(msg -> Mono.error(new RuntimeException("OpenAI error " + r.statusCode() + ": " + msg))))
        .bodyToMono(Map.class)
        .block();

    String result = extractText(resp);
    try {
      return objectMapper.readValue(result, OpenAIStylistRecommendResponse.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse OpenAI JSON", e);
    }
  }

  @SuppressWarnings("unchecked")
  private String extractText(Map<String, Object> resp) {
    if (resp == null) return null;
    try {
      List<Map<String, Object>> output = (List<Map<String, Object>>) resp.get("output");
      if (output != null && !output.isEmpty()) {
        List<Map<String, Object>> content = (List<Map<String, Object>>) output.get(0).get("content");
        if (content != null && !content.isEmpty()) {
          Object text = content.get(0).get("text");
          if (text instanceof String s) return s;
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse text from response", e);
    }
    return null;
  }
}
