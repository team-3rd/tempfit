package com.example.tempfit.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Role;
import com.example.tempfit.repository.MemberRepository;
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

    private Member dtoToEntity(MemberDTO dto){
        Member member = Member.builder()
                        .email(dto.getEmail())
                        .name(dto.getName())
                        .password(dto.getPassword())
                        .sex(dto.getSex())
                        .fromSocial(false)
                        .build();
        member.addMemberRole(Role.USER);
        return member;
    }
}
