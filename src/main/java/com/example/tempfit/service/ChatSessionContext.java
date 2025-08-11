package com.example.tempfit.service;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChatSessionContext implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDateTime lastTargetDT; // 마지막으로 사용한 기준 시각(오늘/내일/오전3시 등)
    private Double lastLat;
    private Double lastLon;
    private String lastLocationLabel;   // "부산 중구" 등 표시용 라벨
}
