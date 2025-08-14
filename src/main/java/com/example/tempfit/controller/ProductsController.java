package com.example.tempfit.controller;

import com.example.tempfit.dto.ProductsDTO;
import com.example.tempfit.service.ProductsService;
import com.example.tempfit.service.ProductsService.FetchResult;
import com.example.tempfit.service.ProductsService.Gender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductsController {

    private final ProductsService productsService;

    @GetMapping("/{gender}")
    public String listByGenderAndItemName(
            @PathVariable String gender,
            @RequestParam(name = "item") String itemName,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        if (itemName == null || itemName.isBlank()) {
            model.addAttribute("errorMessage", "상품 이름(item)이 필요합니다.");
            return "products/list-empty";
        }

        FetchResult result = productsService.fetchByGenderAndDisplayName(gender, itemName);

        if (result.getGender() == Gender.UNKNOWN) {
            model.addAttribute("errorMessage", "지원되지 않는 상품 종류입니다.");
            return "products/list-empty";
        }

        // 페이징: 한 페이지당 10개
        int pageSize = 10;
        int total = result.getProducts().size();
        int totalPages = (total + pageSize - 1) / pageSize;
        int currentPage = Math.max(0, Math.min(page, totalPages - 1));

        int fromIndex = currentPage * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        java.util.List<ProductsDTO> paged = result.getProducts().subList(fromIndex, toIndex);

        model.addAttribute("gender", gender);
        model.addAttribute("genderLabel", gender.equalsIgnoreCase("male") ? "남성" : "여성");
        model.addAttribute("itemName", result.getCategoryDisplayName());
        model.addAttribute("products", paged); // 잘라낸 페이징된 리스트
        model.addAttribute("itemImagePath", result.getGuideImageUrl());

        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        return "products/products";
    }
}
