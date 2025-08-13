package com.example.tempfit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoordiService {

    private final ClothingGuideService clothingGuideService;

    // ===== 리팩토링된: 남/여 DB 랜덤 추천 메서드 =====
    /**
     * gender ("male" 또는 "female"), temp (실제 온도 값) 에 맞춰
     * 각 category별 {name, imageUrl} 맵을 반환합니다.
     */
    public Map<String, Map<String, String>> getRandomClothingWithImage(String gender, int temp) {
        boolean isMale = "male".equalsIgnoreCase(gender);

        if (isMale) {
            // 남성용: category → ClothMale
            return clothingGuideService
                .getRandomMaleClothingByTemperature(temp)
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
            // 여성용: category → ClothFemale
            return clothingGuideService
                .getRandomFemaleClothingByTemperature(temp)
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
