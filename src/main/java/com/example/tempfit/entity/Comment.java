package com.example.tempfit.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "comments")  // Oracle 예약어 회피를 위해 테이블명 명시
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Community post;

    // 익명 댓글용 작성자 이름
    @Column(length = 50)
    private String authorName;

    // Oracle CLOB 매핑
    @Lob
    @Column(nullable = false)
    private String content;

    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
    }
}

