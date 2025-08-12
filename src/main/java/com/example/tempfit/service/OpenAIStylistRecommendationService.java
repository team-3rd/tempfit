// src/main/java/com/example/tempfit/service/OpenAIStylistRecommendationService.java
package com.example.tempfit.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.tempfit.dto.OpenAIStylistNaverShoppingItem;
import com.example.tempfit.dto.OpenAIStylistProductDTO;
import com.example.tempfit.dto.OpenAIStylistRecommendResponse;
import com.example.tempfit.dto.OpenAIStylistRecommendationResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenAIStylistRecommendationService {

    private final OpenAIStylistService openAIService;
    private final OpenAIStylistNaverShoppingService naverShoppingService;

    private static final List<String> CATS = List.of("상의", "아우터", "하의", "신발");
    private static final Random RND = new Random();

    // GET /api/aiguide?temp=27 용
    public Map<String, List<OpenAIStylistRecommendationResult>> recommendBoth(int temp) {
        List<OpenAIStylistRecommendationResult> male   = recommendFor(temp, "남성");
        List<OpenAIStylistRecommendationResult> female = recommendFor(temp, "여성");

        Map<String, List<OpenAIStylistRecommendationResult>> result = new LinkedHashMap<>();
        result.put("male", male);   // 항상 male 먼저
        result.put("female", female);
        return result;
    }

    // POST /api/aiguide (자유 프롬프트 테스트용)
    public List<OpenAIStylistRecommendationResult> recommendFromPrompt(String userPrompt) {
        // 프롬프트 그대로 호출(이 메서드는 테스트/디버그용)
        OpenAIStylistRecommendResponse first = openAIService.getProducts(userPrompt);
        // 기본값: 남성으로 처리(프롬프트에 여성 포함이면 여성으로 전환)
        String koGender = (userPrompt != null && userPrompt.contains("여성")) ? "여성" : "남성";
        int temp = 20; // 임시 기본값
        return pipelineFromOpenAI(first, temp, koGender);
    }

    // ───────────────────────────────────────────────────────────────────
    // 메인 파이프라인(온도/성별 기준) – 하드코딩 백업 없이 OpenAI로 보강
    // ───────────────────────────────────────────────────────────────────
    private List<OpenAIStylistRecommendationResult> recommendFor(int temp, String koGender) {
        OpenAIStylistRecommendResponse first = openAIService.getProducts(buildSetPrompt(temp, koGender));
        return pipelineFromOpenAI(first, temp, koGender);
    }

    private List<OpenAIStylistRecommendationResult> pipelineFromOpenAI(
            OpenAIStylistRecommendResponse first, int temp, String koGender) {

        // 1) 카테고리 맵 구성 + 상의 잘못(셔츠/자켓류) 필터
        Map<String, OpenAIStylistProductDTO> byCat = new LinkedHashMap<>();
        Set<String> usedBrands = new LinkedHashSet<>();
        if (first != null && first.getProducts() != null) {
            for (OpenAIStylistProductDTO p : first.getProducts()) {
                if (p == null || p.getCategory() == null) continue;
                if ("상의".equals(p.getCategory()) && invalidTopName(p.getProductName())) continue;
                byCat.put(p.getCategory(), p);
                if (p.getBrandName() != null) usedBrands.add(p.getBrandName().trim());
            }
        }

        // 2) 빠진 카테고리 보강(최대 3회, 브랜드 중복 방지)
        for (String cat : CATS) {
            if (!byCat.containsKey(cat)) {
                OpenAIStylistProductDTO fixed =
                        requestOneProductWithRetry(temp, koGender, cat, usedBrands, 3, null);
                if (fixed != null) {
                    byCat.put(cat, fixed);
                    if (fixed.getBrandName() != null) usedBrands.add(fixed.getBrandName().trim());
                }
            }
        }

        // 3) 네이버 검색 확보(없으면 대체안 생성 재시도) → 최소 1개 이미지 링크 확보 노력
        List<OpenAIStylistRecommendationResult> out = new ArrayList<>();
        for (String cat : CATS) {
            OpenAIStylistProductDTO prod = byCat.get(cat);
            if (prod == null) continue;

            List<OpenAIStylistNaverShoppingItem> items =
                naverShoppingService.search(
                    ((prod.getBrandName() == null ? "" : prod.getBrandName()) + " " +
                     (prod.getProductName() == null ? "" : prod.getProductName())).trim(),
                    prod.getBrandName(),
                    "여성".equals(koGender) ? "female" : "male"
                );

            int attempts = 0;
            while ((items == null || items.isEmpty()) && attempts < 2) {
                attempts++;
                OpenAIStylistProductDTO alt =
                    requestOneProductWithRetry(temp, koGender, cat, usedBrands, 1,
                        "이전과 다른 브랜드/상품으로 대체안을 한 개만 다시 제안하라.");
                if (alt == null) break;
                prod = alt;
                if (alt.getBrandName() != null) usedBrands.add(alt.getBrandName().trim());

                items = naverShoppingService.search(
                    ((alt.getBrandName() == null ? "" : alt.getBrandName()) + " " +
                     (alt.getProductName() == null ? "" : alt.getProductName())).trim(),
                    alt.getBrandName(),
                    "여성".equals(koGender) ? "female" : "male"
                );
            }

            if (items == null) items = List.of();
            if (items.size() > 1) items = items.subList(0, 1);

            out.add(OpenAIStylistRecommendationResult.builder()
                    .product(prod)
                    .items(items)
                    .build());
        }

        return out;
    }

    // ───────────────────────────────────────────────────────────────────
    // OpenAI 프롬프트
    // ───────────────────────────────────────────────────────────────────
    private String buildSetPrompt(int temp, String koGender) {
        String[] vibes = { "클린", "미니멀", "스마트 캐주얼", "컨템포러리", "세미 포멀" };
        String[] palettes = { "화이트·베이지", "아이보리·그레이", "네이비·화이트", "블랙·그레이", "베이지·네이비" };
        String vibe = vibes[RND.nextInt(vibes.length)];
        String palette = palettes[RND.nextInt(palettes.length)];

        return """
            당신은 전문 패션 스타일리스트다. 아래 조건을 '모두' 지키며
            섭씨 %d도 날씨에 맞는 %s 전용 코디 1세트를
            상의, 아우터, 하의, 신발 4개 카테고리로 빠짐없이 추천하라.
            상의, 아우터, 하의는 현재 유행중인 트렌드한 핏으로 추천하라.
            남성 옷은 남성옷으로, 여성 옷은 여성 옷으로 추천하라.

            공통 규칙:
            - 반드시 '%s 라인(전용)'만 사용. 유니섹스/여성/남성 혼용 금지.
            - 서로 잘 어울리는 하나의 룩으로 제안. 컬러 팔레트는 %s 범위에서 2~3색으로 제한.
            - 트렌드 반영 + 무난/대중성(20%% 트렌드, 80%% 베이식). 최근 2년(2023~현재) 라인 우선.
            - 같은 브랜드를 여러 카테고리에 중복 사용하지 말 것.
            - 브랜드는 다음 중 실제 대중 브랜드만: 유니클로, 자라(ZARA), COS, H&M, 무신사 스탠다드, 탑텐, 스파오, 에잇세컨즈,
              리바이스, 폴로 랄프 로렌, 라코스테, 나이키, 아디다스, 뉴발란스, 컨버스, 반스.
            - 브랜드명은 '판매처/몰명'이 아닌 '제조 브랜드명'이어야 함(무신사 스토어/스마트스토어/쿠팡 등 금지).
            - 상품명은 해당 브랜드 실제 라인명을 사용(예: U 크루넥, 린넨 블렌드 셔츠, 501, 삼바 OG, 척 70 등).
            - 다음 키워드는 금지: 쿨/냉감/메쉬/기능성/러닝/등산/UPF/슬리퍼/샌들/래시가드/골프/자전거/스윔/작업복.
            - 신발은 슬리퍼·샌들이 아닌 스니커즈/로퍼/더비 위주.

            카테고리 규칙:
            - 상의: 티셔츠/니트/폴로/스웨트셔츠만 허용. '셔츠/오버셔츠/셔켓/자켓/재킷/가디건/블레이저' 금지.
            - 아우터: 카디건/오버셔츠/린넨 블레이저/라이트 재킷 등 가벼운 아우터를 반드시 포함(빈값 금지).
            - 하의: 치노/슬랙스/데님/코튼 팬츠/스커트(여성만).
            - 신발: 스니커즈/로퍼/더비/부츠 중 택1. 비오는 날은 부츠/레인부츠.

            출력 형식:
            - 오직 JSON만. 스키마: products[].category(상의/아우터/하의/신발), brandName, productName
            참고 스타일 힌트(가이드): %s
            """.formatted(temp, koGender, koGender, palette, vibe);
    }

    private String buildOnePrompt(int temp, String koGender, String category, Set<String> avoidBrands, String extra) {
        String avoid = (avoidBrands == null || avoidBrands.isEmpty())
                ? "없음"
                : String.join(", ", avoidBrands);

        String topRule = "상의".equals(category)
                ? "상의는 티셔츠/니트/폴로/스웨트셔츠만 허용. '셔츠/오버셔츠/셔켓/자켓/재킷/가디건/블레이저' 금지."
                : "";

        return """
            당신은 패션 스타일리스트다. 섭씨 %d도, %s 전용 코디에서
            '%s' 카테고리의 제품 1개만 추천하라.

            제약:
            - 다음 브랜드는 피하라(이미 사용됨): %s
            - %s
            - 브랜드는 대중 브랜드에서만: 유니클로, 자라(ZARA), COS, H&M, 무신사 스탠다드, 탑텐, 스파오, 에잇세컨즈,
              리바이스, 폴로 랄프 로렌, 라코스테, 나이키, 아디다스, 뉴발란스, 컨버스, 반스.
            - 기능성/스포츠 전용/슬리퍼/샌들/등산/러닝 등 키워드 금지.
            - 오직 JSON만. 스키마: products[].category('%s'), brandName, productName
            %s
            """.formatted(temp, koGender, category, avoid, topRule, category, (extra == null ? "" : extra));
    }

    private OpenAIStylistProductDTO requestOneProductWithRetry(
            int temp, String koGender, String category, Set<String> avoidBrands, int maxTry, String extra) {

        for (int i = 0; i < maxTry; i++) {
            String p = buildOnePrompt(temp, koGender, category, avoidBrands, extra);
            OpenAIStylistRecommendResponse r = openAIService.getProducts(p);
            if (r == null || r.getProducts() == null || r.getProducts().isEmpty()) continue;

            OpenAIStylistProductDTO prod = r.getProducts().get(0);
            if (prod == null) continue;
            if (!category.equals(prod.getCategory())) continue;
            if ("상의".equals(category) && invalidTopName(prod.getProductName())) continue;

            return prod;
        }
        return null;
    }

    // 상의에서 허용되지 않는 토큰(셔츠류/자켓류) – '폴로 셔츠'는 예외
    private static boolean invalidTopName(String name) {
        if (name == null) return false;
        String s = name.toLowerCase();
        if (s.contains("오버셔츠") || s.contains("셔켓") || s.contains("자켓") || s.contains("재킷")
            || s.contains("블레이저") || s.contains("가디건")) return true;
        if (s.contains("셔츠") && !s.contains("폴로")) return true; // 폴로셔츠만 예외
        return false;
    }
}
