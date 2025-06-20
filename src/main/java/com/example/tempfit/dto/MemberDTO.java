package com.example.tempfit.dto;

import com.example.tempfit.entity.Sex;

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
    private String name;
    private Sex sex;
}
