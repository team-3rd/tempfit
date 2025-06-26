package com.example.tempfit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "community_sex")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunitySex {

    @Id
    private Long id;  // Community와 PK 공유

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private Community community;

    @Column(nullable = false)
    private boolean male;

    @Column(nullable = false)
    private boolean female;

    @Version
    private int version;

    // 연관관계 편의 메서드(필수는 아니지만 있으면 좋음)
    public void setCommunity(Community community) {
        this.community = community;
    }
}
