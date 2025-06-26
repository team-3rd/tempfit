package com.example.tempfit.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.tempfit.entity.Sex;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class AuthMemberDTO extends User implements OAuth2User {

    private String email;
    private String password;
    private String name;
    private Sex sex;
    private boolean fromSocial;

    private Map<String, Object> attr;

    public AuthMemberDTO(String username, String name, String password, boolean fromSocial, Sex sex,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.email = username;
        this.name = name;
        this.password = password;
        this.fromSocial = fromSocial;
        this.sex = sex;
    }

    public AuthMemberDTO(String username, String name, String password, boolean fromSocial, Sex sex,
            Collection<? extends GrantedAuthority> authorities, Map<String, Object> attr) {
        this(username, name, password, fromSocial, sex, authorities);
        this.attr = attr;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attr;
    }
}
