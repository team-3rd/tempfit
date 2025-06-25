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

@ToString(exclude = {"dibsList", "likeList"})
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

    private Sex sex;

    private boolean fromSocial;
    
    
    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Role> roleSet = new HashSet<>();

    //@OneToMany(mappedBy = "member")
    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private Set<String> dibsList = new HashSet<>(); //String -> Coordi
    
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Recommend> RecommendSet = new HashSet<>();

    public void addMemberRole(Role role){
        roleSet.add(role);
    }
}
