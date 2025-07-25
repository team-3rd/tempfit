package com.example.tempfit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Sex;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;                // ← 추가된 import
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityDTO {

    private Long id;
    private String title;
    private Member author;
    private String content;
    private int recommendCount;

    private List<Sex> sexSet;
    private boolean male;
    private boolean female;

    private List<String> styleNames;
    private boolean casual;
    private boolean street;
    private boolean formal;
    private boolean outdoor;

    private LocalDate dates;               // LocalDate 타입 사용
    private double minTemp;
    private double maxTemp;
    private double avgTemp;
    private double lat;
    private double lon;

    private MultipartFile repImage;
    private List<MultipartFile> extraImages;

    private String repImageUrl;
    private List<String> extraImageUrls;

    private LocalDateTime createdDate;
    private LocalDateTime upDateTime;

    /**
     * 상대 시간 표시:
     * - 등록 후 1분 미만: "방금 전"
     * - 1분 이상 1시간 미만: "N분 전"
     * - 1시간 이상 24시간 미만: "N시간 전"
     * - 24시간 이상: "MM-dd"
     */
    public String getDisplayDate() {
        LocalDateTime now = LocalDateTime.now();
        Duration diff = Duration.between(createdDate, now);
        long seconds = diff.getSeconds();

        if (seconds < 60) {
            return "방금 전";
        }
        long minutes = diff.toMinutes();
        if (minutes < 60) {
            return minutes + "분 전";
        }
        long hours = diff.toHours();
        if (hours < 24) {
            return hours + "시간 전";
        }
        // 24시간 이상 경과 시 월-일 표시
        return createdDate.format(DateTimeFormatter.ofPattern("MM-dd"));
    }
}
