package com.example.tempfit.security;


import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.tempfit.entity.Member;
import com.example.tempfit.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class MemberDetailsService implements UserDetailsService{

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("username {}", username);
        Member member = memberRepository.findByEmailAndFromSocial(username, false);

        if(member == null)
            throw new UsernameNotFoundException("알맞은 이메일인지 확인해주세요");
        
        AuthMemberDTO authMemberDTO = new AuthMemberDTO(member.getEmail(),
            member.getPassword(), member.isFromSocial(),
            member.getRoleSet().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
        .collect(Collectors.toList()));

        authMemberDTO.setName(member.getName());
        authMemberDTO.setFromSocial(member.isFromSocial());

        return authMemberDTO;
    }

}
