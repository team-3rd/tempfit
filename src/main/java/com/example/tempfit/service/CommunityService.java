package com.example.tempfit.service;

import com.example.tempfit.entity.TemperatureRange;
import com.example.tempfit.dto.CommunityDTO;
import com.example.tempfit.dto.SelectedWeatherDTO;
import com.example.tempfit.entity.Bookmark;
import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.CommunityImage;
import com.example.tempfit.entity.CommunitySex;
import com.example.tempfit.entity.CommunityStyle;
import com.example.tempfit.entity.CommunityTemp;
import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Recommend;
import com.example.tempfit.repository.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommunityService {

    private final MemberRepository memberRepository;

    private final CommunityRepository communityRepository;
    private final CommunityStyleRepository communityStyleRepository;
    private final CommunitySexRepository communitySexRepository;
    private final CommunityImageRepository communityImageRepository;
    private final RecommendRepository recommendRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CommentRepository commentRepository;
    private final CommunityTempRepository communityTempRepository;

    @Value("${upload.path}")
    private String uploadDir;

    // 게시글 등록 + 이미지 저장 (대표 인덱스로 구분)
    public Long register(CommunityDTO dto,
            Member currentUser,
            List<MultipartFile> imageFiles,
            int repImageIndex,
            List<SelectedWeatherDTO> weatherData) throws IOException {

        Community community = Community.builder()
                .title(dto.getTitle())
                .author(currentUser)
                .content(dto.getContent())
                .recommendCount(dto.getRecommendCount())
                .build();
        communityRepository.save(community);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile file = imageFiles.get(i);
                if (!file.isEmpty()) {
                    boolean isRep = (i == repImageIndex);
                    saveCommunityImage(community, file, isRep);
                }
            }
        }

        CommunitySex sex = CommunitySex.builder()
                .male(dto.isMale())
                .female(dto.isFemale())
                .build();

        CommunityStyle style = CommunityStyle.builder()
                .casual(dto.isCasual())
                .street(dto.isStreet())
                .formal(dto.isFormal())
                .outdoor(dto.isOutdoor())
                .build();

        List<Integer> tmps = new ArrayList<>();
        List<String> ptys = new ArrayList<>();
        List<String> skys = new ArrayList<>();
        for (SelectedWeatherDTO data : weatherData) {
            tmps.add((int) data.getTmp());
            ptys.add(data.getPty());
            skys.add(data.getSky());
        }

        for (int i = 0; i < skys.size(); i++) {
            if ("맑음".equals(skys.get(i))) {
                dto.setSky("맑음");
            } else if ("구름 많음".equals(skys.get(i))) {
                dto.setSky("구름 많음");
            } else if ("흐림".equals(skys.get(i)) && "강수없음".equals(ptys.get(i))) {
                dto.setSky("흐림");
            } else if ("비".equals(ptys.get(i))) {
                dto.setSky("비");
            }
        }

        int mins = tmps.stream().mapToInt(Integer::intValue).min().orElse(Integer.MIN_VALUE);
        int maxs = tmps.stream().mapToInt(Integer::intValue).max().orElse(Integer.MAX_VALUE);
        dto.setMinTemp(mins);
        dto.setMaxTemp(maxs);
        dto.setAvgTemp((mins + maxs) / 2);

        CommunityTemp temp = CommunityTemp.builder()
                .dates(dto.getDates())
                .minTemp(dto.getMinTemp())
                .maxTemp(dto.getMaxTemp())
                .avgTemp(dto.getAvgTemp())
                .sky(dto.getSky())
                .build();

        sex.setCommunity(community);
        style.setCommunity(community);
        temp.setCommunity(community);
        community.setCommunitySex(sex);
        community.setCommunityStyle(style);
        community.setCommunityTemp(temp);

        communitySexRepository.save(sex);
        communityStyleRepository.save(style);
        communityTempRepository.save(temp);
        communityRepository.save(community);

        return community.getId();
    }

    public CommunityDTO get(Long id) {
        Community entity = communityRepository.findById(id).orElseThrow();
        CommunityDTO dto = entityToDTO(entity);

        List<CommunityImage> imgs = communityImageRepository.findByCommunity_IdOrderByIsRepDescIdAsc(id);
        if (!imgs.isEmpty()) {
            dto.setRepImageUrl(imgs.get(0).getFileName());
            dto.setExtraImageUrls(imgs.stream()
                    .skip(1)
                    .map(CommunityImage::getFileName)
                    .collect(Collectors.toList()));
            dto.setHasMultiImages(imgs.size() > 1);
        }

        communityStyleRepository.findById(id).ifPresent(style -> {
            dto.setCasual(style.isCasual());
            dto.setStreet(style.isStreet());
            dto.setFormal(style.isFormal());
            dto.setOutdoor(style.isOutdoor());
        });

        communityTempRepository.findById(id).ifPresent(temp -> {
            dto.setMinTemp(temp.getMinTemp());
            dto.setMaxTemp(temp.getMaxTemp());
            dto.setAvgTemp(temp.getAvgTemp());
            dto.setSky(temp.getSky());
        });

        memberRepository.findByEmail(entity.getAuthor().getEmail()).ifPresent(member -> {
            dto.setProfileImageUrl(member.getProfileImageUrl());
        });
        return dto;
    }

    public Page<CommunityDTO> getPage(int page) {
        Pageable pageable = PageRequest.of(page - 1, 10,
                Sort.by(Sort.Direction.DESC, "createdDate"));
        return communityRepository.list(null, null, null, null, pageable)
                .map(this::arrayToDTO);
    }

    public Page<CommunityDTO> searchPage(String type, String keyword, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10,
                Sort.by(Sort.Direction.DESC, "createdDate"));
        return communityRepository.list(type, keyword, null, null, pageable)
                .map(this::arrayToDTO);
    }

    public Page<CommunityDTO> searchPageRaw(String type,
            String keyword,
            List<String> styleNames,
            int page) {
        Pageable pageable = PageRequest.of(page - 1, 10,
                Sort.by(Sort.Direction.DESC, "createdDate"));
        return communityRepository.list(type, keyword, styleNames, null, pageable)
                .map(this::arrayToDTO);
    }

    public void remove(Long id) {
        Community post = communityRepository.findById(id).get();
        commentRepository.deleteAll(commentRepository.findByPostIdOrderByCreatedDateAsc(id));
        recommendRepository.deleteAll(recommendRepository.findByCommunity(post));
        communityTempRepository.deleteById(id);
        communityStyleRepository.deleteById(id);
        communitySexRepository.deleteById(id);
        communityImageRepository.deleteAll(communityImageRepository.findByCommunity_IdOrderByIsRepDescIdAsc(id));
        post.setCommunityTemp(null);
        post.setCommunityStyle(null);
        post.setCommunitySex(null);
        communityRepository.save(post);
        communityRepository.delete(post);
    }

    private void saveCommunityImage(Community community,
            MultipartFile file,
            boolean isRep) throws IOException {
        File uploadPathDir = new File(uploadDir);
        if (!uploadPathDir.exists())
            uploadPathDir.mkdirs();

        String uuid = UUID.randomUUID().toString();
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String storedName = uuid + (ext != null ? "." + ext : "");
        File dest = new File(uploadPathDir, storedName);
        file.transferTo(dest);

        CommunityImage img = CommunityImage.builder()
                .community(community)
                .origName(file.getOriginalFilename())
                .fileName(storedName)
                .isRep(isRep)
                .build();
        communityImageRepository.save(img);
    }

    public CommunityDTO entityToDTO(Community entity) {
        List<CommunityImage> imgs = communityImageRepository
                .findByCommunity_IdOrderByIsRepDescIdAsc(entity.getId());
        String repUrl = (!imgs.isEmpty()) ? imgs.get(0).getFileName() : null;

        CommunityTemp temp = entity.getCommunityTemp();
        Member member = entity.getAuthor();

        return CommunityDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .author(entity.getAuthor())
                .content(entity.getContent())
                .recommendCount(entity.getRecommendCount())
                .profileImageUrl(member.getProfileImageUrl())
                .repImageUrl(repUrl)
                .male(entity.getCommunitySex() != null && entity.getCommunitySex().isMale())
                .female(entity.getCommunitySex() != null && entity.getCommunitySex().isFemale())
                .casual(entity.getCommunityStyle() != null && entity.getCommunityStyle().isCasual())
                .street(entity.getCommunityStyle() != null && entity.getCommunityStyle().isStreet())
                .formal(entity.getCommunityStyle() != null && entity.getCommunityStyle().isFormal())
                .outdoor(entity.getCommunityStyle() != null && entity.getCommunityStyle().isOutdoor())
                .minTemp(temp != null ? temp.getMinTemp() : 0)
                .maxTemp(temp != null ? temp.getMaxTemp() : 0)
                .avgTemp(temp != null ? temp.getAvgTemp() : 0)
                .sky(temp != null ? temp.getSky() : "")
                .createdDate(entity.getCreatedDate())
                .upDateTime(entity.getUpDateTime())
                .viewCount(entity.getViewCount())
                .build();
    }

    private CommunityDTO arrayToDTO(Object[] arr) {
        return CommunityDTO.builder()
                .id((Long) arr[0])
                .title((String) arr[1])
                .author((Member) arr[2])
                .content((String) arr[3])
                .recommendCount((Integer) arr[4])
                .repImageUrl((String) arr[5])
                .createdDate((LocalDateTime) arr[6])
                .profileImageUrl((String) arr[7])
                .casual((Boolean) arr[8])
                .street((Boolean) arr[9])
                .formal((Boolean) arr[10])
                .outdoor((Boolean) arr[11])
                .minTemp((int) arr[12])
                .maxTemp((int) arr[13])
                .avgTemp((int) arr[14])
                .sky((String) arr[15])
                .viewCount((Integer) arr[16])
                .build();
    }

    @Transactional(readOnly = true)
    public List<CommunityDTO> getPostsByMember(Member member) {
        List<Community> posts = communityRepository.findByAuthor(member);
        return posts.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    // 추천/취소
    @Transactional
    public void recommendPost(Long communityId, Member member) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        Optional<Recommend> existRec = recommendRepository.findByMemberAndCommunity(member, community);
        if (existRec.isPresent()) {
            recommendRepository.delete(existRec.get());
            community.setRecommendCount(community.getRecommendCount() - 1);
        } else {
            Recommend rec = Recommend.builder()
                    .community(community)
                    .member(member)
                    .build();
            recommendRepository.save(rec);
            community.setRecommendCount(community.getRecommendCount() + 1);
        }
        communityRepository.save(community);
    }

    @Transactional
    public void bookmarkPost(Long communityId, Member member) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        Optional<Bookmark> existBook = bookmarkRepository.findByMemberAndCommunity(member, community);
        if (existBook.isPresent()) {
            bookmarkRepository.delete(existBook.get());
        } else {
            Bookmark rec = Bookmark.builder()
                    .community(community)
                    .member(member)
                    .build();
            bookmarkRepository.save(rec);
        }
        communityRepository.save(community);
    }

    @Transactional(readOnly = true)
    public List<CommunityDTO> getPostsByIds(List<Long> bookmarkIds) {
        List<Community> bookmarkedPosts = communityRepository.findByIdIn(bookmarkIds);

        List<CommunityDTO> dtoList = bookmarkedPosts.stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
        return dtoList;
    }

    @Transactional
    public void increaseViewCount(Long id) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));
        community.setViewCount(community.getViewCount() + 1);
        communityRepository.save(community);
    }

    // ===== 리스트 표시용 사용자 상태/이미지 개수 세팅 =====
    @Transactional(readOnly = true)
    public void applyUserFlags(List<CommunityDTO> dtos, Member me) {
        for (CommunityDTO dto : dtos) {
            // 이미지 여러 장 여부
            List<CommunityImage> imgs = communityImageRepository
                    .findByCommunity_IdOrderByIsRepDescIdAsc(dto.getId());
            dto.setHasMultiImages(imgs != null && imgs.size() > 1);

            // 로그인 O일 때만 좋아요/북마크 표시
            if (me != null) {
                Community ref = communityRepository.getReferenceById(dto.getId());
                boolean liked = recommendRepository.findByMemberAndCommunity(me, ref).isPresent();
                boolean bookmarked = bookmarkRepository.findByMemberAndCommunity(me, ref).isPresent();
                dto.setLikedByMe(liked);
                dto.setBookmarkedByMe(bookmarked);
            } else {
                dto.setLikedByMe(false);
                dto.setBookmarkedByMe(false);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<CommunityDTO> getTopPostsByTemp(int temp, int limit) {
        // 1) 온도 구간 매핑
        TemperatureRange range = TemperatureRange.fromTemperature(temp);

        // 2) 주간 로테이션을 위해 상위 풀(pool) 넉넉히 확보
        // (추천수 desc, 작성일 desc 기준 상위 N개 중에서 4개만 주마다 회전)
        final int poolSize = Math.max(limit * 5, 20);

        Specification<Community> spec = (root, query, cb) -> {
            Join<Community, CommunityTemp> t = root.join("communityTemp");
            // avgTemp가 현재 온도 구간에 들어오는 글만
            return cb.between(t.get("avgTemp"), range.getMinTemp(), range.getMaxTemp());
        };

        Pageable pageable = PageRequest.of(
                0,
                poolSize,
                Sort.by(Sort.Direction.DESC, "recommendCount")
                        .and(Sort.by(Sort.Direction.DESC, "createdDate")));

        List<CommunityDTO> pool = communityRepository.findAll(spec, pageable)
                .getContent()
                .stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());

        // 3) 풀 크기가 4 이하라면 그대로 반환
        if (pool.size() <= limit) {
            return pool;
        }

        // 4) 주차(weekOfWeekBasedYear) 기반 고정 로테이션
        // → 같은 주에는 동일 결과, 주가 바뀌면 다른 구간으로 이동
        int week = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear());
        int start = week % (pool.size() - limit + 1);

        return new ArrayList<>(pool.subList(start, start + limit));
    }
}
