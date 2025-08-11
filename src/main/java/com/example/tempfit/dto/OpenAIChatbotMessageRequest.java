package com.example.tempfit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenAIChatbotMessageRequest {
    @NotBlank
    private String message;

    private Double temperature; // 섭씨, null 허용
    private String location; // 지역명, null 허용
    private String date; // ISO yyyy-MM-dd, null 허용

    private Double lat; // 위도, null 허용
    private Double lon; // 경도, null 허용

    private Integer sexCode; // DB 성별코드

    private String gender; // 챗봇 문자열 성별
}
