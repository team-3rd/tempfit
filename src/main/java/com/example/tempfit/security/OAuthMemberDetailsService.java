package com.example.tempfit.security;


import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Role;
import com.example.tempfit.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class OAuthMemberDetailsService extends DefaultOAuth2UserService{

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{
        log.info("userRequest {}",userRequest);

        String clientName = userRequest.getClientRegistration().getClientName();
        System.out.println("==========");
        log.info("clientName {}", clientName);
        log.info(userRequest.getAdditionalParameters());
        log.info("========= OAuth2User의 값");

        OAuth2User oAuth2User = super.loadUser(userRequest);
        oAuth2User.getAttributes().forEach((k,v)-> {
            log.info("{}:{}",k,v);
        });

        String email = null;
        if(clientName.equals("Google")){
            email = oAuth2User.getAttribute("email");
        }

        Member member = saveSocialMember(email);

        AuthMemberDTO authMemberDTO = new AuthMemberDTO(member.getEmail(), member.getPassword(), member.isFromSocial(),
        member.getRoleSet().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).collect(Collectors.toList()),
        oAuth2User.getAttributes());
        authMemberDTO.setName(member.getName());
        authMemberDTO.setSex(member.getSex());

        return authMemberDTO;
    }

    private Member saveSocialMember(String email){
        Member member = memberRepository.findByEmailAndFromSocial(email, true);
        if(member == null){
            Member saveMember = Member.builder()
                        .email(email)
                        .name(email)
                        .password(passwordEncoder.encode("1111"))
                        .fromSocial(true)
                        .build();

                    saveMember.addMemberRole(Role.USER);
                    memberRepository.save(saveMember);
                    return saveMember;
        }
        return member;
    }
}
