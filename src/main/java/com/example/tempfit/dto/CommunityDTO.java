package com.example.tempfit.dto;

import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Sex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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
    private String profileImageUrl;

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

    private LocalDate dates;
    private int minTemp;
    private int maxTemp;
    private int avgTemp;
    private String sky;

    private double lat;
    private double lon;

    private MultipartFile repImage;
    private List<MultipartFile> extraImages;

    private String repImageUrl;
    private List<String> extraImageUrls;

    private LocalDateTime createdDate;
    private LocalDateTime upDateTime;

    private int viewCount; // 조회수

    // ▼ 리스트에서 사용할 사용자별 상태
    private boolean likedByMe;      // 내가 추천(찜)했는지
    private boolean bookmarkedByMe; // 내가 북마크했는지
    private boolean hasMultiImages; // 이미지가 여러 장인지

    /**
     * 상대 시간 표시:
     * - 1시간 미만: "N분 전"
     * - 24시간 미만: "N시간 전"
     * - 30일 미만: "N일 전"
     * - 30일 이상: "MM-dd"
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
        long days = diff.toDays();
        if (days < 30) {
            return days + "일 전";
        }
        return createdDate.format(DateTimeFormatter.ofPattern("MM-dd"));
    }
}
