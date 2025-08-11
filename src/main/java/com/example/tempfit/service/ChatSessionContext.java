package com.example.tempfit.service;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatSessionContext implements Serializable {
    private LocalDateTime lastTargetDT; // 마지막 기준 시각
    private Double lastLat;
    private Double lastLon;
    private String lastLocationLabel;   // "부산 중구" 등 표시용
    private String lastOutfitText;      // ★ 직전 의상 추천 전체 문장(색 추천 시 매칭에 사용)
}
