package com.example.tempfit.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.tempfit.entity.Member;

public class LoginMemberDetails implements UserDetails {
    private final Member member;

    public LoginMemberDetails(Member member) {
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
       return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }
}
