package com.example.tempfit.dto;

import java.util.List;

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
public class OpenAIStylistRecommendationResult {
    private OpenAIStylistProductDTO product;
    private List<OpenAIStylistNaverShoppingItem> items;
}
