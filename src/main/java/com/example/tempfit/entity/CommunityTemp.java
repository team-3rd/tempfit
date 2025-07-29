package com.example.tempfit.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "community_temp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityTemp {

    @Id
    private Long id; // Community와 PK 공유

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Community community;

    @Column(nullable = false)
    private LocalDate dates;

    @Column(nullable = false)
    private int minTemp;

    @Column(nullable = false)
    private int maxTemp;

    @Column(nullable = false)
    private int avgTemp;

    @Column(nullable = false)
    private String sky;

    // 연관관계 편의 메서드(필수는 아니지만 있으면 좋음)
    public void setCommunity(Community community) {
        this.community = community;
    }
}
