package com.example.tempfit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIStylistRecommendRequest {
    private String prompt; // 사용자 프롬프트
}
