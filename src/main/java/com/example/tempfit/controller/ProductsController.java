package com.example.tempfit.controller;

import com.example.tempfit.dto.ProductsDTO;
import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Products;
import com.example.tempfit.repository.MemberRepository;
import com.example.tempfit.repository.ProductsRepository;
import com.example.tempfit.security.AuthMemberDTO;
import com.example.tempfit.service.MemberService;
import com.example.tempfit.service.ProductsService;
import com.example.tempfit.service.ProductsService.FetchResult;
import com.example.tempfit.service.ProductsService.Gender;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductsController {

    private final ProductsRepository productsRepository;
    private final ProductsService productsService;
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @GetMapping("/{gender}")
    public String listByGenderAndItemName(
            @PathVariable String gender,
            @RequestParam(name = "item") String itemName,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @AuthenticationPrincipal AuthMemberDTO authMemberDTO,
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

        if (authMemberDTO != null) {
        Member member = memberRepository.findByEmail(authMemberDTO.getEmail())
                                        .orElse(null);
        if (member != null) {
            List<Long> dibIds = member.getDibsList()
                                      .stream()
                                      .map(Products::getProductId)
                                      .collect(Collectors.toList());
            model.addAttribute("dibIds", dibIds);
        }
    }

        return "products/products";
    }

    @PostMapping("/dibs/{id}")
    @ResponseBody
    public Map<String, Object> addDiblist(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthMemberDTO authMemberDTO) throws Exception {

        if (authMemberDTO == null) return Map.of("ok", false, "reason", "UNAUTHORIZED");
        Products product = productsRepository.findByProductId(id);
        Member member = memberRepository.findByEmail(authMemberDTO.getEmail()).get();
        boolean nowActive;
        
            if (member.getDibsList().contains(product)) {
                nowActive = false;
            }
            else{nowActive = true;}
            memberService.addDibs(id, member);
        return Map.of("ok", true, "active", nowActive);
    }
}
