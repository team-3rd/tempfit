package com.example.tempfit.dto;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import com.example.tempfit.entity.Sex;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private String email;
    private String password;
    @NotBlank
    private String name;
    @NotBlank
    private String nickname;
    private Sex sex;
    private List<CommunityDTO> myPosts;
    private List<CommentDTO> myComments;
    private String profileImageUrl;
    private Integer personalColorCode;

    @Value("${default.profile-image-url}")
    private String defaultProfileImageUrl;

    public int getPersonalColorCode() {
        return personalColorCode != null ? personalColorCode : 0;
    }

    public int getPostCount() {
        return myPosts != null ? myPosts.size() : 0;
    }

    public int getCommentCount() {
        return myComments != null ? myComments.size() : 0;
    }

    public String getProfileImageUrl() {
        return profileImageUrl != null ? profileImageUrl : defaultProfileImageUrl;
    }
}
