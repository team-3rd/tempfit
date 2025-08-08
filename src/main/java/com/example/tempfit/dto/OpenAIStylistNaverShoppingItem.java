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
public class OpenAIStylistNaverShoppingItem {
    private String title;
    private String link;
    private String image;
    private String lprice;
    private String mallName;
}
