package com.example.tempfit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Table(name = "COORDI")
public class Coordi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 50)
    private String author;

    @Column(length = 200)
    private String imageUrl; // 이미지 URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemperatureRange temperatureRange;

    // 기존 enum Style 필드 제거

    // CommunityStyle 엔티티로 1:1 매핑
    @OneToOne(
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    @JoinColumn(name = "style_flags_id")
    private CommunityStyle communityStyle;

    @Column(nullable = false)
    private int recommendCount;

    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        createdDate = LocalDateTime.now();
    }

    // 편의 메서드: 스타일 플래그 설정
    public void setCommunityStyle(CommunityStyle flags) {
        this.communityStyle = flags;
    }
}
