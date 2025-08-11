package com.example.tempfit.service;

import com.example.tempfit.entity.Member;
import com.example.tempfit.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonalColorService {

    private final MemberRepository memberRepository;

    @Transactional
    public int saveChoice(String email, int code) {
        Member m = memberRepository.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다: " + email));
        m.updatePersonalColorCode(code); // 0~4 외는 0으로 정규화
        return m.getPersonalColorCode() == null ? 0 : m.getPersonalColorCode();
    }

    @Transactional
    public int getChoice(String email) {
        return memberRepository.findById(email)
                .map(m -> m.getPersonalColorCode() == null ? 0 : m.getPersonalColorCode())
                .orElse(0);
    }
}
