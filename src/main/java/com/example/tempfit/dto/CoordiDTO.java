package com.example.tempfit.dto;

import com.example.tempfit.entity.TemperatureRange;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoordiDTO {
    private Long id;
    private String title;
    private String author;
    private String imageUrl;
    private TemperatureRange temperatureRange;

    // 스타일 플래그
    private boolean casual;
    private boolean street;
    private boolean formal;
    private boolean outdoor;

    private int recommendCount;
    private LocalDateTime createdDate;
}
