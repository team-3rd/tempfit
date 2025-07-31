package com.example.tempfit.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.tempfit.entity.ProductsMale;
import com.example.tempfit.repository.ClothFemaleRepository;
import com.example.tempfit.repository.ClothMaleRepository;
import com.example.tempfit.service.CoordiService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final CoordiService coordiService;
    private final ClothMaleRepository clothMaleRepository;
    private final ClothFemaleRepository clothFemaleRepository;

    @GetMapping("/{gender}_{itemName}")
    public String listByGenderAndItemName(
            @PathVariable String gender,
            @PathVariable String itemName,
            Model model) {

        // 1) 슬러그 역복원: '-' → ' ', '+' → '/'
        String displayName = itemName
                .replaceAll("-+", " ")
                .replaceAll("\\++", "/");

        // 2) 카테고리(의상명) 기준으로 상품 리스트 조회
        List<ProductsMale> products = coordiService.getProductsByCategory(displayName);

        // 3) 이미지 URL 조회 (엔티티 필드명이 clothName 이므로 메서드명도 그에 맞춰 수정)
        String imagePath;
        if ("female".equalsIgnoreCase(gender)) {
            imagePath = clothFemaleRepository.findImageUrlByClothName(displayName);
        } else {
            imagePath = clothMaleRepository.findImageUrlByClothName(displayName);
        }

        // 4) 뷰에 전달
        model.addAttribute("gender", gender);
        model.addAttribute("genderLabel", gender.equalsIgnoreCase("male") ? "남성" : "여성");
        model.addAttribute("itemName", displayName);
        model.addAttribute("products", products);
        model.addAttribute("itemImagePath", imagePath);

        return "products/list";
    }
}
