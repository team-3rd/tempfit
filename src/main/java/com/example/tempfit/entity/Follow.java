package com.example.tempfit.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_follow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 팔로우 하는 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_email", nullable = false)
    private Member follower;

    // 팔로우 받는 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_email", nullable = false)
    private Member following;

    @Column(nullable = false, updatable = false)
    private LocalDateTime followedAt;

    @PrePersist
    public void prePersist() {
        this.followedAt = LocalDateTime.now();
    }
}
