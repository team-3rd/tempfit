package com.example.tempfit.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.tempfit.entity.Product;
import com.example.tempfit.service.CoordiService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final CoordiService coordiService;

    @GetMapping("/{productKey}")
    public String listByProductKey(
            @PathVariable("productKey") String productKey,
            Model model
    ) {
        List<Product> products = coordiService.getProductsForProductKey(productKey);
        model.addAttribute("productKey", productKey);
        model.addAttribute("products", products);
        return "products/list";
    }
}