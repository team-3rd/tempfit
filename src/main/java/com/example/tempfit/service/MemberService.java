package com.example.tempfit.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import com.example.tempfit.entity.Comment;
import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Role;
import com.example.tempfit.repository.MemberRepository;
import com.example.tempfit.security.AuthMemberDTO;
import com.example.tempfit.security.LoginMemberDetails;
import com.example.tempfit.dto.CommentDTO;
import com.example.tempfit.dto.CommunityDTO;
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

        List<CommunityDTO> posts = member.getMyPosts().stream()
        .sorted(Comparator.comparing(Community::getCreatedDate).reversed()) // 최신순 정렬
        .map(post -> CommunityDTO.builder()
        .id(post.getId())
        .title(post.getTitle())
        .content(post.getContent())
        .createdDate(post.getCreatedDate())
        .recommendCount(post.getRecommendCount())
        .build())
        .collect(Collectors.toList());

        List<CommentDTO> comments = member.getMyComments().stream()
        .sorted(Comparator.comparing(Comment::getCreatedDate).reversed())
        .map(comment -> CommentDTO.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .createdDate(comment.getCreatedDate())
        .postId(comment.getPost().getId())   // 댓글이 달린 게시글 ID도 같이
        .build())
        .collect(Collectors.toList());

        return entityToDTO(member, posts, comments);
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
        AuthMemberDTO authMemberDTO = new AuthMemberDTO(email, updatedUserDetails.getName(), updatedUserDetails.getNickname(), updatedUserDetails.getPassword(), updatedUserDetails.isFromSocial(), updatedUserDetails.getSex(), updatedUserDetails.getAuthorities());

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
        authMemberDTO,
        null,
        authMemberDTO.getAuthorities()
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

     private MemberDTO entityToDTO(Member member, List<CommunityDTO> posts, List<CommentDTO> comments) {
        return MemberDTO.builder()
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .sex(member.getSex())
                .myPosts(posts)
                .myComments(comments)
                .build();
    }
}
