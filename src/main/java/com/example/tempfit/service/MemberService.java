package com.example.tempfit.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;

import com.example.tempfit.entity.Comment;
import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.CommunityImage;
import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Products;
import com.example.tempfit.entity.Recommend;
import com.example.tempfit.entity.Role;
import com.example.tempfit.repository.MemberRepository;
import com.example.tempfit.repository.ProductsRepository;
import com.example.tempfit.security.AuthMemberDTO;
import com.example.tempfit.security.LoginMemberDetails;
import com.example.tempfit.dto.CommentDTO;
import com.example.tempfit.dto.CommunityDTO;
import com.example.tempfit.dto.MemberDTO;
import com.example.tempfit.dto.ProductsDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final ProductsRepository productsRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${upload.path}" + "/profile/")
    private String uploadDir;

    @Value("${default.profile-image-url}")
    private String defaultProfileImageUrl;

    public String register(MemberDTO dto)
    {
        Member member = dtoToEntity(dto);
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setProfileImageUrl(defaultProfileImageUrl);
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

        if (member.getProfileImageUrl() == null) {
            member.setProfileImageUrl(defaultProfileImageUrl);
        }

        LoginMemberDetails updatedUserDetails = new LoginMemberDetails(member);
        AuthMemberDTO authMemberDTO = new AuthMemberDTO(email, updatedUserDetails.getName(), updatedUserDetails.getNickname(), updatedUserDetails.getPassword(), updatedUserDetails.isFromSocial(), updatedUserDetails.getSex(), updatedUserDetails.getProfileImage(), updatedUserDetails.getAuthorities());

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
    
    public List<ProductsDTO> getDibList(String email)
    {
        Member member = memberRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
        List<Products> dibList = new ArrayList<>(member.getDibsList());
         List<ProductsDTO> dtoList = dibList.stream()
            .map(p -> new ProductsDTO(p.getProductId(), p.getBrandName(), p.getProductName(), p.getImageUrl(), p.getLinkUrl()))
                    .collect(Collectors.toList());
        return dtoList;
    }

    @Transactional
    public void addDibs(Long productId, Member member) throws Exception {
        if(member == null) throw new Exception("유저를 찾을 수 없습니다.");
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        if (member.getDibsList().contains(product)) {
            member.getDibsList().remove(product);
        } else {
            member.getDibsList().add(product);
        }
        memberRepository.save(member);
    }

    public void updateProfileImage(String email, MultipartFile file) throws IOException {
        Member member = memberRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        // 기존 이미지 삭제 (기본 이미지 제외)
        if (member.getProfileImageUrl() != null &&
            !member.getProfileImageUrl().equals(defaultProfileImageUrl)) {

            String oldFileName = member.getProfileImageUrl()
                    .replace("http://localhost:8080/uploads/profile/", "");
            File oldFile = new File(uploadDir, oldFileName);
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }

        // 새 이미지 저장
        String storedName = saveProfileImage(file);

        // 새 URL 설정
        String imageUrl = "http://localhost:8080/uploads/profile/" + storedName;
        member.setProfileImageUrl(imageUrl);

        memberRepository.save(member);
    }

    public void resetToDefaultProfileImage(String email) {
        Member member = memberRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        String currentImageUrl = member.getProfileImageUrl();

        if (currentImageUrl != null && !currentImageUrl.equals(defaultProfileImageUrl)) {
            try {
                // URL → 파일 경로 변환
                String fileName = currentImageUrl.replace("/uploads/profile/", "");
                File fileToDelete = new File(uploadDir, fileName);

                if (fileToDelete.exists()) {
                    boolean deleted = fileToDelete.delete();
                    if (!deleted) {
                        System.err.println("⚠ 파일 삭제 실패: " + fileToDelete.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // DB 기본 이미지로 변경
        member.setProfileImageUrl(defaultProfileImageUrl);
        memberRepository.save(member);
    }

    private String saveProfileImage(MultipartFile file) throws IOException {
        File uploadPathDir = new File(uploadDir);
        if (!uploadPathDir.exists())
            uploadPathDir.mkdirs();

        String uuid = UUID.randomUUID().toString();
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String storedName = uuid + (ext != null ? "." + ext : "");
        File dest = new File(uploadPathDir, storedName);
        file.transferTo(dest);

        return storedName;
    }

    private Member dtoToEntity(MemberDTO dto){
        Member member = Member.builder()
                        .email(dto.getEmail())
                        .name(dto.getName())
                        .nickname(dto.getNickname())
                        .password(dto.getPassword())
                        .sex(dto.getSex())
                        .fromSocial(false)
                        .profileImageUrl(defaultProfileImageUrl)
                        .dibsList(null)
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
                .profileImageUrl((member.getProfileImageUrl() != null && !member.getProfileImageUrl().isEmpty())
                                ? member.getProfileImageUrl()
                                : defaultProfileImageUrl)
                .build();
    }
}
