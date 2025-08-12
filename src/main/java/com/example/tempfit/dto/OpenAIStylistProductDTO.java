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
    private String category;     // 상의/아우터/하의/신발
    private String brandName;    // ← 변경: brand -> brandName
    private String productName;  // ← 변경: name  -> productName
    private String keyword;      // (옵션) 이미지/검색용 키워드 - 사용하지 않음
}
