package com.example.tempfit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "community_style")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityStyle {

    @Id
    private Long id;  // Community와 PK 공유

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Community community;

    @Column(nullable = false)
    private boolean casual;

    @Column(nullable = false)
    private boolean street;

    @Column(nullable = false)
    private boolean formal;

    @Column(nullable = false)
    private boolean outdoor;

    @Version
    private int version;

    // 연관관계 편의 메서드(필수는 아니지만 있으면 좋음)
    public void setCommunity(Community community) {
        this.community = community;
    }
}
