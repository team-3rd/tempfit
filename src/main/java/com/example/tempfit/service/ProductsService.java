package com.example.tempfit.service;

import com.example.tempfit.dto.ProductsDTO;
import com.example.tempfit.entity.ClothFemale;
import com.example.tempfit.entity.ClothMale;
import com.example.tempfit.entity.ProductsFemale;
import com.example.tempfit.entity.ProductsMale;
import com.example.tempfit.repository.ClothFemaleRepository;
import com.example.tempfit.repository.ClothMaleRepository;
import com.example.tempfit.repository.ProductsFemaleRepository;
import com.example.tempfit.repository.ProductsMaleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductsService {

    private static final Logger log = LoggerFactory.getLogger(ProductsService.class);

    private final ProductsMaleRepository productsMaleRepository;
    private final ProductsFemaleRepository productsFemaleRepository;
    private final ClothMaleRepository clothMaleRepository;
    private final ClothFemaleRepository clothFemaleRepository;

    public enum Gender {
        MALE, FEMALE, UNKNOWN
    }

    public static class FetchResult {
        private final Gender gender;
        private final String categoryDisplayName;
        private final String guideImageUrl; // 의상 가이드 이미지
        private final List<ProductsDTO> products;

        public FetchResult(Gender gender, String categoryDisplayName, String guideImageUrl, List<ProductsDTO> products) {
            this.gender = gender;
            this.categoryDisplayName = categoryDisplayName;
            this.guideImageUrl = guideImageUrl;
            this.products = products;
        }

        public Gender getGender() {
            return gender;
        }

        public String getCategoryDisplayName() {
            return categoryDisplayName;
        }

        public String getGuideImageUrl() {
            return guideImageUrl;
        }

        public List<ProductsDTO> getProducts() {
            return products;
        }
    }

    /**
     * 한글 displayName 그대로 (예: "린넨 셔츠") 받아서 처리
     */
    public FetchResult fetchByGenderAndDisplayName(String gender, String displayName) {
        Gender g = determineGender(gender);
        String normalized = displayName.trim();

        if (g == Gender.MALE) {
            Optional<ClothMale> clothOpt = clothMaleRepository.findByClothNameIgnoreCase(normalized);
            if (clothOpt.isEmpty()) {
                log.debug("남성 의상 가이드 매핑 실패: cloth_name='{}'", normalized);
                return new FetchResult(Gender.UNKNOWN, normalized, null, Collections.emptyList());
            }
            ClothMale cloth = clothOpt.get();
            String guideImg = cloth.getImageUrl(); // 의상 가이드 이미지
            String categoryId = String.valueOf(cloth.getClothId()); // 숫자지만 repos가 String이라면

            List<ProductsMale> maleList = productsMaleRepository.findByCategoryIdIgnoreCase(categoryId);
            List<ProductsDTO> dtos = maleList.stream()
                    .map(m -> new ProductsDTO(m.getBrandName(), m.getProductName(), m.getImageUrl(), m.getLinkUrl()))
                    .collect(Collectors.toList());

            return new FetchResult(Gender.MALE, cloth.getClothName(), guideImg, dtos);

        } else if (g == Gender.FEMALE) {
            Optional<ClothFemale> clothOpt = clothFemaleRepository.findByClothNameIgnoreCase(normalized);
            if (clothOpt.isEmpty()) {
                log.debug("여성 의상 가이드 매핑 실패: cloth_name='{}'", normalized);
                return new FetchResult(Gender.UNKNOWN, normalized, null, Collections.emptyList());
            }
            ClothFemale cloth = clothOpt.get();
            String guideImg = cloth.getImageUrl();
            String categoryId = String.valueOf(cloth.getClothId());

            List<ProductsFemale> femaleList = productsFemaleRepository.findByCategoryIdIgnoreCase(categoryId);
            List<ProductsDTO> dtos = femaleList.stream()
                    .map(f -> new ProductsDTO(f.getBrandName(), f.getProductName(), f.getImageUrl(), f.getLinkUrl()))
                    .collect(Collectors.toList());

            return new FetchResult(Gender.FEMALE, cloth.getClothName(), guideImg, dtos);
        } else {
            return new FetchResult(Gender.UNKNOWN, normalized, null, Collections.emptyList());
        }
    }

    private Gender determineGender(String slug) {
        if (slug == null) return Gender.UNKNOWN;
        String lower = slug.toLowerCase(Locale.ROOT);
        if (lower.startsWith("male") || lower.startsWith("men")) return Gender.MALE;
        if (lower.startsWith("female") || lower.startsWith("women")) return Gender.FEMALE;
        return Gender.UNKNOWN;
    }
}
