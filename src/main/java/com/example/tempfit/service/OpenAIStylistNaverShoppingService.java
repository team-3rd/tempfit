// src/main/java/com/example/tempfit/service/OpenAIStylistNaverShoppingService.java
package com.example.tempfit.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
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

    // 예: https://openapi.naver.com/v1/search/shop.json
    @Value("${naver.base-url}")
    private String baseUrl;

    // 넉넉히 가져와 필터 후 1개만 반환
    private static final int FETCH_SIZE = 10;

    // 기능성/비일상 배제
    private static final String[] BANNED_KEYWORDS = {
        "쿨", "냉감", "메쉬", "기능성", "러닝", "등산", "upf", "uv", "래시가드",
        "아쿠아", "골프", "싸이클", "자전거", "스윔", "슬리퍼", "슬라이드", "쪼리", "조리", "작업복"
    };

    // 성별 토큰
    private static final String[] MALE_TOKENS   = { "남성", "남자", "맨", "men", "man's", "mens", "male" };
    private static final String[] FEMALE_TOKENS = { "여성", "여자", "우먼", "women", "woman", "womens", "female", "ladies" };

    /**
     * @param keyword      "브랜드명 상품명"
     * @param brandFilter  브랜드 가중치용(우선 정렬)
     * @param gender       "male" | "female"
     * @return 0~N개(호출측에서 1개로 슬라이스)
     */
    public List<OpenAIStylistNaverShoppingItem> search(String keyword, String brandFilter, String gender) {
        if (keyword != null) {
            // 미완성 퍼센트 이스케이프 제거(인코딩 오류 예방)
            keyword = keyword.replaceAll("%(?![0-9a-fA-F]{2})", "");
        }

        // 1차: 브랜드+상품 + 성별 힌트
        List<OpenAIStylistNaverShoppingItem> first = runQuery(keyword, brandFilter, gender, true);
        if (!first.isEmpty()) return first.subList(0, 1);

        // 2차: 상품만(브랜드 제거) + 성별 힌트
        String productOnly = removeLeadingBrand(keyword, brandFilter);
        if (productOnly != null && !productOnly.isBlank()) {
            List<OpenAIStylistNaverShoppingItem> second = runQuery(productOnly, null, gender, true);
            if (!second.isEmpty()) return second.subList(0, 1);
        }

        // 3차: 브랜드+상품 (성별 힌트 제거)
        List<OpenAIStylistNaverShoppingItem> third = runQuery(keyword, brandFilter, gender, false);
        if (!third.isEmpty()) return third.subList(0, 1);

        // 4차: 상품만 (성별 힌트 제거)
        if (productOnly != null && !productOnly.isBlank()) {
            List<OpenAIStylistNaverShoppingItem> fourth = runQuery(productOnly, null, gender, false);
            if (!fourth.isEmpty()) return fourth.subList(0, 1);
        }

        // 5차: 브랜드만 + 성별 힌트(최후 수단)
        if (brandFilter != null && !brandFilter.isBlank()) {
            List<OpenAIStylistNaverShoppingItem> fifth = runQuery(brandFilter, brandFilter, gender, true);
            if (!fifth.isEmpty()) return fifth.subList(0, 1);
        }

        return List.of(); // 호출측에서 OpenAI 재요청으로 대체
    }

    private List<OpenAIStylistNaverShoppingItem> runQuery(String query, String brandFilter, String gender, boolean withGenderHint) {
        if (query == null || query.isBlank()) return List.of();

        String q = query;
        if (withGenderHint) q += " " + ("female".equals(gender) ? "여성" : "남성");

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Naver-Client-Id", clientId)
                .defaultHeader("X-Naver-Client-Secret", clientSecret)
                .build();

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("query", q)
                .queryParam("display", FETCH_SIZE)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        Map<String, Object> resp = client.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, r -> r.bodyToMono(String.class)
                        .map(msg -> new RuntimeException("Naver API error " + r.statusCode() + ": " + msg)))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        List<Map<String, Object>> items = Collections.emptyList();
        if (resp != null) {
            Object raw = resp.get("items");
            if (raw instanceof List<?> list) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> casted = list.stream()
                        .filter(Map.class::isInstance)
                        .map(e -> (Map<String, Object>) e)
                        .collect(Collectors.toList());
                items = casted;
            }
        }

        final String brandNorm = norm(brandFilter);

        // 1) 브랜드 우선(brandFilter 없으면 전체)
        List<Map<String, Object>> byBrand = (brandFilter == null || brandFilter.isBlank())
                ? items
                : items.stream()
                       .filter(m -> containsNorm((String) m.get("title"), brandNorm))
                       .collect(Collectors.toList());

        // 2) 기능성/비일상 필터
        List<Map<String, Object>> filtered = byBrand.stream()
                .filter(m -> !hasBanned((String) m.get("title")))
                .collect(Collectors.toList());

        // 3) 성별 필터(반대 성별 키워드 포함 시 제외)
        List<Map<String, Object>> genderOk = filtered.stream()
                .filter(m -> matchGender((String) m.get("title"), gender))
                .collect(Collectors.toList());

        List<Map<String, Object>> finalList = !genderOk.isEmpty() ? genderOk
                : (!filtered.isEmpty() ? filtered : byBrand);

        return finalList.stream()
                .map(m -> OpenAIStylistNaverShoppingItem.builder()
                        .title((String) m.get("title"))
                        .link((String) m.get("link"))
                        .image((String) m.get("image"))
                        .lprice((String) m.get("lprice"))
                        .mallName((String) m.get("mallName"))
                        .build())
                .collect(Collectors.toList());
    }

    private static String removeLeadingBrand(String keyword, String brand) {
        if (keyword == null || brand == null) return keyword;
        String k = keyword.trim();
        String b = brand.trim();
        if (k.toLowerCase(Locale.ROOT).startsWith(b.toLowerCase(Locale.ROOT) + " ")) {
            return k.substring(b.length()).trim();
        }
        return keyword;
    }

    private static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    private static boolean containsNorm(String title, String needleNorm) {
        if (needleNorm == null || needleNorm.isBlank()) return true;
        if (title == null) return false;
        return title.toLowerCase(Locale.ROOT).contains(needleNorm);
    }

    private static boolean hasBanned(String title) {
        if (title == null) return false;
        String t = title.toLowerCase(Locale.ROOT);
        for (String k : BANNED_KEYWORDS) {
            if (t.contains(k)) return true;
        }
        return false;
    }

    private static boolean matchGender(String title, String gender) {
        if (title == null || gender == null) return true; // 정보 없으면 통과
        String t = title.toLowerCase(Locale.ROOT);
        boolean hasMale = containsAny(t, MALE_TOKENS);
        boolean hasFemale = containsAny(t, FEMALE_TOKENS);

        if ("male".equals(gender)) {
            if (hasFemale) return false;
            return true;
        } else {
            if (hasMale) return false;
            return true;
        }
    }

    private static boolean containsAny(String text, String[] tokens) {
        for (String s : tokens) {
            if (text.contains(s.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }
}
