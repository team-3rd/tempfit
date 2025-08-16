package com.example.tempfit.service;

import com.example.tempfit.dto.ProductsDTO;
import com.example.tempfit.entity.ClothFemale;
import com.example.tempfit.entity.ClothMale;
import com.example.tempfit.entity.Products;
import com.example.tempfit.repository.ClothFemaleRepository;
import com.example.tempfit.repository.ClothMaleRepository;
import com.example.tempfit.repository.ProductsRepository;
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

    private final ProductsRepository productsRepository;      // 단일 products 리포지토리
    private final ClothMaleRepository clothMaleRepository;
    private final ClothFemaleRepository clothFemaleRepository;

    public enum Gender {
        MALE, FEMALE, UNKNOWN
    }

    public static class FetchResult {
        private final Gender gender;
        private final String categoryDisplayName;
        private final String guideImageUrl;
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
     * gender: "male"/"female" 또는 "0"/"1"도 허용
     */
    public FetchResult fetchByGenderAndDisplayName(String gender, String displayName) {
        Gender g = determineGender(gender);
        String normalized = displayName == null ? "" : displayName.trim();

        if (g == Gender.MALE) {
            Optional<ClothMale> clothOpt = clothMaleRepository.findByClothNameIgnoreCase(normalized);
            if (clothOpt.isEmpty()) {
                log.debug("남성 의상 가이드 매핑 실패: cloth_name='{}'", normalized);
                return new FetchResult(Gender.UNKNOWN, normalized, null, Collections.emptyList());
            }
            ClothMale cloth = clothOpt.get();
            String guideImg = cloth.getImageUrl();
            String categoryId = String.valueOf(cloth.getClothId()); // products.category_id 와 매핑

            // ★ gender=0(남성) + categoryId 로 단일 테이블에서 조회
            List<Products> rows = productsRepository.findByGenderAndCategoryIdIgnoreCase(0, categoryId);

            List<ProductsDTO> dtos = rows.stream()
                    .map(p -> new ProductsDTO(p.getProductId(), p.getBrandName(), p.getProductName(), p.getImageUrl(), p.getLinkUrl()))
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

            // ★ gender=1(여성) + categoryId 로 단일 테이블에서 조회
            List<Products> rows = productsRepository.findByGenderAndCategoryIdIgnoreCase(1, categoryId);

            List<ProductsDTO> dtos = rows.stream()
                    .map(p -> new ProductsDTO(p.getProductId(), p.getBrandName(), p.getProductName(), p.getImageUrl(), p.getLinkUrl()))
                    .collect(Collectors.toList());

            return new FetchResult(Gender.FEMALE, cloth.getClothName(), guideImg, dtos);
        } else {
            return new FetchResult(Gender.UNKNOWN, normalized, null, Collections.emptyList());
        }
    }

    private Gender determineGender(String slug) {
        if (slug == null) return Gender.UNKNOWN;
        String lower = slug.toLowerCase(Locale.ROOT).trim();
        if (lower.startsWith("male") || lower.startsWith("men") || lower.equals("0")) return Gender.MALE;
        if (lower.startsWith("female") || lower.startsWith("women") || lower.equals("1")) return Gender.FEMALE;
        return Gender.UNKNOWN;
    }
}
