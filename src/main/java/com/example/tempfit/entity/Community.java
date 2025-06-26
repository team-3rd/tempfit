package com.example.tempfit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /** 제목은 반드시 입력되도록 유지 */
    @Column(nullable = false)
    private String title;

    /* author도 비워둘 수 있게 nullable=true (기본값) */
    @JoinColumn(name = "author_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;

    /** content 역시 nullable=true (기본값), CLOB 매핑 유지 */
    @Lob
    @Column(columnDefinition = "CLOB")
    private String content;

    @Column(nullable = false)
    private int recommendCount;

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
    }

    @PreUpdate
    public void preUpdate() {
        this.upDateTime = LocalDateTime.now();
    }

    // @Column(name = "temperature")
    // private Integer temperature;
}
