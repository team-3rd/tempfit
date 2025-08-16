package com.example.tempfit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductsDTO {
    private Long id;
    private String brandName;
    private String productName;
    private String imageUrl;
    private String linkUrl;
}
