package com.example.tempfit.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Role;
import com.example.tempfit.repository.MemberRepository;
import com.example.tempfit.security.LoginMemberDetails;
import com.example.tempfit.dto.MemberDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public String register(MemberDTO dto)
    {
        Member member = dtoToEntity(dto);
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        Member newMember = memberRepository.save(member);
        return newMember.getEmail();
    }

    public MemberDTO getMember(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("회원 정보를 찾을 수 없습니다."));
        return entityToDTO(member);
    }

    public void update(String email, MemberDTO dto)
    {
        Member member = memberRepository.findByEmail(email).orElseThrow();
        
        Set<Role> originalRoles = new HashSet<>(member.getRoleSet());

        member.setName(dto.getName());
        member.setNickname(dto.getNickname());
        member.setSex(dto.getSex());
        member.setRoleSet(originalRoles);
        memberRepository.save(member);

        LoginMemberDetails updatedUserDetails = new LoginMemberDetails(member);

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
        updatedUserDetails,
        null,
        updatedUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    public boolean checkPw(String id, String pw){
        return true;
    }

    private Member dtoToEntity(MemberDTO dto){
        Member member = Member.builder()
                        .email(dto.getEmail())
                        .name(dto.getName())
                        .nickname(dto.getNickname())
                        .password(dto.getPassword())
                        .sex(dto.getSex())
                        .fromSocial(false)
                        .build();
        member.addMemberRole(Role.USER);
        return member;
    }

     private MemberDTO entityToDTO(Member member) {
        return MemberDTO.builder()
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .sex(member.getSex())
                .build();
    }
}
