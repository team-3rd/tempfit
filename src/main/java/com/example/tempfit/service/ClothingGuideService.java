package com.example.tempfit.service;

import com.example.tempfit.entity.ClothMale;
import com.example.tempfit.entity.ClothFemale;
import com.example.tempfit.entity.ClothingGuideMale;
import com.example.tempfit.entity.ClothingGuideFemale;
import com.example.tempfit.entity.TemperatureRange;  // import 추가
import com.example.tempfit.repository.ClothingGuideMaleRepository;
import com.example.tempfit.repository.ClothingGuideFemaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClothingGuideService {

    private final ClothingGuideMaleRepository maleGuideRepository;
    private final ClothingGuideFemaleRepository femaleGuideRepository;
    private final Random random = new Random();

    /**
     * 실제 온도(℃)에 맞춰 남성용 의상 4개를 랜덤으로 뽑아 Map으로 반환합니다.
     */
    public Map<String, ClothMale> getRandomMaleClothingByTemperature(int temperature) {
        // enum 으로 범위 코드 구하기
        int code = TemperatureRange.fromTemperature(temperature).getCode();
        // 기존 메소드 재활용
        return getRandomMaleClothing(code);
    }

    /**
     * 실제 온도(℃)에 맞춰 여성용 의상 4개를 랜덤으로 뽑아 Map으로 반환합니다.
     */
    public Map<String, ClothFemale> getRandomFemaleClothingByTemperature(int temperature) {
        int code = TemperatureRange.fromTemperature(temperature).getCode();
        return getRandomFemaleClothing(code);
    }

    /**
     * 온도 범위 코드에 맞춰 남성용 의상 4개를 랜덤으로 뽑아 Map으로 반환합니다.
     * key: category (outer, top, bottom, shoes)
     * value: ClothMale
     */
    public Map<String, ClothMale> getRandomMaleClothing(int tempRange) {
        List<ClothingGuideMale> guides = maleGuideRepository.findByTempRange(tempRange);
        List<ClothMale> items = guides.stream()
                                       .map(ClothingGuideMale::getClothMale)
                                       .collect(Collectors.toList());

        return pickOneFromEachCategory(items);
    }

    /**
     * 온도 범위 코드에 맞춰 여성용 의상 4개를 랜덤으로 뽑아 Map으로 반환합니다.
     * key: category (outer, top, bottom, shoes)
     * value: ClothFemale
     */
    public Map<String, ClothFemale> getRandomFemaleClothing(int tempRange) {
        List<ClothingGuideFemale> guides = femaleGuideRepository.findByTempRange(tempRange);
        List<ClothFemale> items = guides.stream()
                                         .map(ClothingGuideFemale::getClothFemale)
                                         .collect(Collectors.toList());

        return pickOneFromEachCategoryFemale(items);
    }

    // 남성용 helper
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

    // 여성용 helper
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
}
