package com.example.tempfit.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString(exclude = { "dibsList", "RecommendSet" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Member {

    @Id
    private String email;

    private String password;

    @NotNull
    private String name;

    @NotNull
    private String nickname;

    private Sex sex;

    private boolean fromSocial;

    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Role> roleSet = new HashSet<>();

    // @OneToMany(mappedBy = "member")
    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Community> dibsList = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Community> myPosts = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> myComments = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Recommend> RecommendSet = new HashSet<>();

    @Column(length = 1000)
    private String profileImageUrl;

    public void addMemberRole(Role role) {
        roleSet.add(role);
    }

    // 퍼스널 컬러 코드, 0 = 미선택(dummy), 1 = 봄 웜톤, 2 = 여름 쿨톤, 3 = 가을 웜톤, 4 = 겨울 쿨톤
    @Builder.Default
    @Column(name = "personal_color_code", columnDefinition = "NUMBER(1) DEFAULT 0", nullable = false)
    private Integer personalColorCode = 0;

    // 퍼스널 컬러 코드 저장(허용 범위: 0~4, 범위 밖/NULL → 0으로 정규화)
    public void updatePersonalColorCode(Integer code) {
        int c = (code == null) ? 0 : code;
        if (c < 0 || c > 4) c = 0;
        this.personalColorCode = c;
    }

}
