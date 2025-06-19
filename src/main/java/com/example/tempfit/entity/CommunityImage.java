package com.example.tempfit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "community_image")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommunityImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // N:1 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    // 저장된 파일명(UUID + 확장자)
    @Column(nullable = false)
    private String fileName;

    // 원본 파일명 (선택)
    private String origName;

    // 대표 이미지 여부 (true=대표, false=추가)
    @Column(nullable = false)
    private boolean isRep;
}

