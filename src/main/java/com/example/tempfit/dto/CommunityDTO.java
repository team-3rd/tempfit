package com.example.tempfit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import com.example.tempfit.entity.Member;

import java.time.LocalDateTime;
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

    /** 스타일 체크박스 값 */
    private List<String> styleNames;
    private boolean casual;
    private boolean street;
    private boolean formal;
    private boolean outdoor;

    /** 업로드용 MultipartFile 필드 */
    private MultipartFile repImage;
    private List<MultipartFile> extraImages;

    /** 출력용 파일 URL 필드 */
    private String repImageUrl;
    private List<String> extraImageUrls;

    private LocalDateTime createdDate;
    private LocalDateTime upDateTime;
}
