package com.example.tempfit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenAIStylistProductDTO {
    private String category; // 상의/아우터/하의/신발
    private String brand;
    private String name;
    private String keyword; // 이미지/검색용 키워드
}

// import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @JsonIgnoreProperties(ignoreUnknown = true)
// public class OpenAIStylistProductDTO {
//     private String category;
//     private String brandName;
//     private String productName;
//     private String imageUrl;
//     private String linkUrl;
// }
