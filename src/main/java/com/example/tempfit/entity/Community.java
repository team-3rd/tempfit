package com.example.tempfit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "COMMUNITY")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = { "communityStyle", "images", "communityTemp" })
@Getter
@Setter
public class Community extends Base {

    @Id
    @Column(name = "post_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "community_seq_gen")
    @SequenceGenerator(name = "community_seq_gen", sequenceName = "COMMUNITY_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Member author;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String content;

    @Column(nullable = false)
    private int recommendCount;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "gender_id")
    private CommunitySex communitySex;

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("isRep DESC, id ASC")
    private List<CommunityImage> images = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "style_flags_id")
    private CommunityStyle communityStyle;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "temp_id")
    private CommunityTemp communityTemp;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    @Column(nullable = false)
    private LocalDateTime upDateTime;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdDate = now;
        this.upDateTime = now;
        // 제목 자동 보정 (INSERT 직전)
        this.title = computeAutoTitle(this.title, this.content);
    }

    @PreUpdate
    public void preUpdate() {
        this.upDateTime = LocalDateTime.now();
        // 제목 자동 보정 (UPDATE 직전)
        this.title = computeAutoTitle(this.title, this.content);
    }

    // ─────────────────────────────
    // 내부 유틸: 제목 자동 생성
    // ─────────────────────────────
    private static String computeAutoTitle(String rawTitle, String rawContent) {
        String title = trimToNull(rawTitle);
        if (title != null) {
            return title; // 사용자가 유효한 제목을 준 경우
        }

        String content = Objects.toString(rawContent, "");
        // 간단한 태그 제거 + 공백 정리
        String plain = content
                .replaceAll("<[^>]*>", " ") // 태그 제거
                .replaceAll("&nbsp;", " ") // nbsp 치환
                .replaceAll("\\s+", " ") // 공백 정규화
                .trim();

        if (plain.isEmpty()) {
            // Oracle은 ""를 NULL로 취급하므로 절대 빈 문자열을 넣지 말 것
            return "제목없음";
        }
        return plain.length() > 30 ? plain.substring(0, 30) : plain;
    }

    private static String trimToNull(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
