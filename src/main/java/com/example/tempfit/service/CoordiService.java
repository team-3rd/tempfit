// src/main/java/com/example/tempfit/service/CoordiService.java
package com.example.tempfit.service;

import com.example.tempfit.entity.ClothFemale;
import com.example.tempfit.entity.ClothMale;
import com.example.tempfit.entity.ClothingGuideFemale;
import com.example.tempfit.entity.ClothingGuideMale;
import com.example.tempfit.entity.TemperatureRange;
import com.example.tempfit.repository.ClothingGuideFemaleRepository;
import com.example.tempfit.repository.ClothingGuideMaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoordiService {

    // ────────────────── Repositories (남/여 의상 가이드) ──────────────────
    private final ClothingGuideMaleRepository maleGuideRepository;
    private final ClothingGuideFemaleRepository femaleGuideRepository;

    private final Random random = new Random();

    // ────────────────── 남성 추천 (실제 온도 → 범위코드) ──────────────────
    /**
     * 실제 온도(℃)에 맞춰 남성용 의상 4개를 랜덤으로 뽑아 Map으로 반환.
     * key: category (top, outer, bottom, shoes) / value: ClothMale
     */
    public Map<String, ClothMale> getRandomMaleClothingByTemperature(int temperature) {
        int code = TemperatureRange.fromTemperature(temperature).getCode();
        return getRandomMaleClothing(code);
    }

    // ────────────────── 여성 추천 (실제 온도 → 범위코드) ──────────────────
    /**
     * 실제 온도(℃)에 맞춰 여성용 의상 4개를 랜덤으로 뽑아 Map으로 반환.
     * key: category (top, outer, bottom, shoes) / value: ClothFemale
     */
    public Map<String, ClothFemale> getRandomFemaleClothingByTemperature(int temperature) {
        int code = TemperatureRange.fromTemperature(temperature).getCode();
        return getRandomFemaleClothing(code);
    }

    // ────────────────── 남성 추천 (온도범위 코드) ──────────────────
    /**
     * 온도 범위 코드에 맞춰 남성용 의상 4개를 랜덤으로 뽑아 Map으로 반환.
     * key: category (outer, top, bottom, shoes) / value: ClothMale
     */
    public Map<String, ClothMale> getRandomMaleClothing(int tempRange) {
        List<ClothingGuideMale> guides = maleGuideRepository.findByTempRange(tempRange);
        List<ClothMale> items = guides.stream()
                .map(ClothingGuideMale::getClothMale)
                .collect(Collectors.toList());
        return pickOneFromEachCategory(items);
    }

    // ────────────────── 여성 추천 (온도범위 코드) ──────────────────
    /**
     * 온도 범위 코드에 맞춰 여성용 의상 4개를 랜덤으로 뽑아 Map으로 반환.
     * key: category (outer, top, bottom, shoes) / value: ClothFemale
     */
    public Map<String, ClothFemale> getRandomFemaleClothing(int tempRange) {
        List<ClothingGuideFemale> guides = femaleGuideRepository.findByTempRange(tempRange);
        List<ClothFemale> items = guides.stream()
                .map(ClothingGuideFemale::getClothFemale)
                .collect(Collectors.toList());
        return pickOneFromEachCategoryFemale(items);
    }

    // ────────────────── 공용 헬퍼 (남성) ──────────────────
    private Map<String, ClothMale> pickOneFromEachCategory(List<ClothMale> items) {
        Map<String, List<ClothMale>> byCat = items.stream()
                .collect(Collectors.groupingBy(ClothMale::getCategory));
        Map<String, ClothMale> result = new LinkedHashMap<>();
        for (String cat : List.of("top", "outer", "bottom", "shoes")) {
            List<ClothMale> list = byCat.getOrDefault(cat, Collections.emptyList());
            if (!list.isEmpty()) {
                result.put(cat, list.get(random.nextInt(list.size())));
            }
        }
        return result;
    }

    // ────────────────── 공용 헬퍼 (여성) ──────────────────
    private Map<String, ClothFemale> pickOneFromEachCategoryFemale(List<ClothFemale> items) {
        Map<String, List<ClothFemale>> byCat = items.stream()
                .collect(Collectors.groupingBy(ClothFemale::getCategory));
        Map<String, ClothFemale> result = new LinkedHashMap<>();
        for (String cat : List.of("top", "outer", "bottom", "shoes")) {
            List<ClothFemale> list = byCat.getOrDefault(cat, Collections.emptyList());
            if (!list.isEmpty()) {
                result.put(cat, list.get(random.nextInt(list.size())));
            }
        }
        return result;
    }

    // ────────────────── 외부 제공용: 남/여 랜덤 의상(name, imageUrl) ──────────────────
    /**
     * gender ("male" 또는 "female"), temp (실제 온도 값)에 맞춰
     * 각 category별 {name, imageUrl} 맵을 반환.
     * 반환 예:
     * {
     *   "top":    {"name": "...", "imageUrl": "..."},
     *   "outer":  {"name": "...", "imageUrl": "..."},
     *   "bottom": {"name": "...", "imageUrl": "..."},
     *   "shoes":  {"name": "...", "imageUrl": "..."}
     * }
     */
    public Map<String, Map<String, String>> getRandomClothingWithImage(String gender, int temp) {
        boolean isMale = "male".equalsIgnoreCase(gender);

        if (isMale) {
            return getRandomMaleClothingByTemperature(temp)
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> Map.of(
                                    "name", e.getValue().getClothName(),
                                    "imageUrl", e.getValue().getImageUrl()
                            ),
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));
        } else {
            return getRandomFemaleClothingByTemperature(temp)
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> Map.of(
                                    "name", e.getValue().getClothName(),
                                    "imageUrl", e.getValue().getImageUrl()
                            ),
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));
        }
    }
}
