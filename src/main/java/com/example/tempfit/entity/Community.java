package com.example.tempfit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    }

    @PreUpdate
    public void preUpdate() {
        this.upDateTime = LocalDateTime.now();
    }
}
