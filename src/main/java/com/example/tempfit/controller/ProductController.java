package com.example.tempfit.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.tempfit.entity.Product;
import com.example.tempfit.guide.ClothingGuideFemale;
import com.example.tempfit.guide.ClothingGuideMale;
import com.example.tempfit.service.CoordiService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final CoordiService coordiService;

    @GetMapping("/{gender}_{itemName}")
    public String listByGenderAndItemName(
            @PathVariable String gender,
            @PathVariable String itemName,
            Model model
    ) {
        // 1) 슬러그 역복원: '-' → ' ', '+' → '/'
        String displayName = itemName
            .replaceAll("-+", " ")
            .replaceAll("\\++", "/");

        // 2) DB 조회
        List<Product> products =
            coordiService.getProductsByGenderAndItemName(gender, displayName);

        // 3) 이미지 맵에서 꺼내오기
        String imagePath;
        if ("female".equalsIgnoreCase(gender)) {
            imagePath = ClothingGuideFemale.imageUrlMap.get(displayName);
        } else {
            imagePath = ClothingGuideMale.imageUrlMap.get(displayName);
        }

        // 4) 뷰에 전달
        model.addAttribute("gender",        gender);
        model.addAttribute("genderLabel",   gender.equals("male") ? "남성" : "여성");
        model.addAttribute("itemName",      displayName);
        model.addAttribute("products",      products);
        model.addAttribute("itemImagePath", imagePath);

        return "products/list";
    }

}