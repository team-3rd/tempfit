package com.example.tempfit.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.tempfit.dto.OpenAIStylistRecommendRes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

  public OpenAIStylistRecommendRes getProducts(String userPrompt) {
    WebClient client = WebClient.builder()
        .baseUrl(openAiBaseUrl)
        .defaultHeader("Authorization", "Bearer " + openAiKey)
        .build();

    Map<String, Object> schema = Map.of(
        "type", "object",
        "properties", Map.of(
            "products", Map.of(
                "type", "array",
                "items", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "category", Map.of("type", "string"),
                        "brand", Map.of("type", "string"),
                        "name", Map.of("type", "string"),
                        "keyword", Map.of("type", "string")),
                    "required", List.of("category", "brand", "name", "keyword"),
                    "additionalProperties", false))),
        "required", List.of("products"),
        "additionalProperties", false);

    Map<String, Object> body = new HashMap<>();
    body.put("model", model);

    body.put("text", Map.of(
        "format", Map.of(
            "type", "json_schema",
            "name", "ProductList",
            "strict", true,
            "schema", schema)));

    body.put("input", List.of(
        Map.of("role", "system", "content", """
                You are a helpful product recommendation assistant.
                Return ONLY a JSON that matches the provided schema.
            """),
        Map.of("role", "user", "content", userPrompt)));

    // call Responses API
    Map<String, Object> resp = client.post()
        .uri("/responses")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .onStatus(HttpStatusCode::isError,
            r -> r.bodyToMono(String.class)
                .flatMap(msg -> Mono.error(new RuntimeException("OpenAI error " + r.statusCode() + ": " + msg))))
        .bodyToMono(Map.class)
        .block();

    System.out.println(("=================="));
    System.out.println(resp);

    System.out.println(("=================="));
    String result = extractText(resp);
    System.out.println(result);

    try {
      return objectMapper.readValue(result, OpenAIStylistRecommendRes.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse OpenAI JSON", e);
    }
  }

  private String extractText(Map<String, Object> resp) {
    if (resp == null)
      return null;

    try {
      List<Map<String, Object>> output = (List<Map<String, Object>>) resp.get("output");
      if (output != null && !output.isEmpty()) {
        List<Map<String, Object>> content = (List<Map<String, Object>>) output.get(0).get("content");
        if (content != null && !content.isEmpty()) {
          Object text = content.get(0).get("text");
          if (text instanceof String s) {
            return s;
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse text from response", e);
    }
    return null;
  }
}
