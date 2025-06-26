package com.example.tempfit.security;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Role;
import com.example.tempfit.entity.Sex;
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
        String accessToken = userRequest.getAccessToken().getTokenValue();
        System.out.println("==========");
        log.info("clientName {}", clientName);
        log.info(userRequest.getAdditionalParameters());
        log.info("========= OAuth2User의 값");

        OAuth2User oAuth2User = super.loadUser(userRequest);
        oAuth2User.getAttributes().forEach((k,v)-> {
            log.info("{}:{}",k,v);
        });

        String email = null;
        String name = null;
        if(clientName.equals("Google")){
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        }
        if (name == null || name.isBlank()) {
            name = email.split("@")[0];
        }

        Sex sex = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        String url = "https://people.googleapis.com/v1/people/me?personFields=genders";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(url,HttpMethod.GET,entity,Map.class);

        String gender = null;
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> genders = (List<Map<String, Object>>) body.get("genders");

            if (genders != null && !genders.isEmpty()) {
                gender = (String) genders.get(0).get("value"); // "male" or "female"
                log.info("Google 계정 성별: {}", gender);

                if (gender.equals("male"))sex = Sex.MALE;
                else if (gender.equals("female"))sex = Sex.FEMALE;
            }
        }

        Member member = saveSocialMember(email, name, sex);

        AuthMemberDTO authMemberDTO = new AuthMemberDTO(member.getEmail(), member.getName(), member.getPassword(), member.isFromSocial(), member.getSex(),
        member.getRoleSet().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).collect(Collectors.toList()),
        oAuth2User.getAttributes());

        return authMemberDTO;
    }

    private Member saveSocialMember(String email, String name, Sex sex){
        Member member = memberRepository.findByEmailAndFromSocial(email, true);
        if(member == null){
            Member saveMember = Member.builder()
                        .email(email)
                        .name(name)
                        .password(passwordEncoder.encode("1111"))
                        .sex(sex)
                        .fromSocial(true)
                        .build();
                    saveMember.addMemberRole(Role.USER);
                    memberRepository.save(saveMember);
                    return saveMember;
        }
        return member;
    }
}
