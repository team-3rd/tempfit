package com.example.tempfit.service;

import com.example.tempfit.dto.CommunityDTO;
import com.example.tempfit.dto.SelectedWeatherDTO;
import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.CommunityImage;
import com.example.tempfit.entity.CommunitySex;
import com.example.tempfit.entity.CommunityStyle;
import com.example.tempfit.entity.CommunityTemp;
import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Recommend;
import com.example.tempfit.entity.Sex;
import com.example.tempfit.repository.CommentRepository;
import com.example.tempfit.entity.TemperatureRange;
import com.example.tempfit.repository.CommunityImageRepository;
import com.example.tempfit.repository.CommunityRepository;
import com.example.tempfit.repository.CommunitySexRepository;
import com.example.tempfit.repository.CommunityStyleRepository;
import com.example.tempfit.repository.CommunityTempRepository;
import com.example.tempfit.repository.RecommendRepository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityStyleRepository communityStyleRepository;
    private final CommunitySexRepository communitySexRepository;
    private final CommunityImageRepository communityImageRepository;
    private final RecommendRepository recommendRepository;
    private final CommentRepository commentRepository;
    private final CommunityTempRepository communityTempRepository;

    @Value("${upload.path}")
    private String uploadDir;

    // 게시글 등록 + 이미지 저장
    public Long register(CommunityDTO dto, Member currentUser, MultipartFile repImage, List<MultipartFile> extraImages,
            List<SelectedWeatherDTO> weatherList)
            throws IOException {
        // Community 일단 저장
        Community community = Community.builder()
                .title(dto.getTitle())
                .author(currentUser)
                .content(dto.getContent())
                .recommendCount(dto.getRecommendCount())
                .build();
        communityRepository.save(community);

        saveCommunityImage(community, repImage, true); // 대표사진 분류
        if (extraImages != null) { // 대표사진 확인
            for (MultipartFile mf : extraImages) { // 추가사진 저장
                if (!mf.isEmpty()) {
                    saveCommunityImage(community, mf, false); // 저장
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

        // 평균 기온 계산 후 날씨 객체 생성
        double daySum = 0, nightSum = 0;
        int dayCount = 0, nightCount = 0;

        sex.setCommunity(community);
        community.setCommunitySex(sex);

        for (int i = 0; i < weatherList.size(); i++) {
            int hour = weatherList.get(i).getFcstTime().getHour();
            double tmp = Double.parseDouble(weatherList.get(i).getTmp());

            if (hour >= 6 && hour <= 17) {
                daySum += tmp;
                dayCount++;
            }

            if (hour >= 18 || hour <= 5) {
                nightSum += tmp;
                nightCount++;
            }
        }

        Double dayAvg = dayCount > 0 ? daySum / dayCount : 0;
        Double nightAvg = nightCount > 0 ? nightSum / nightCount : 0;

        dto.setDayAvgTemp(dayAvg);
        dto.setNightAvgTemp(nightAvg);

        CommunityTemp temp = CommunityTemp.builder()
                .dates(dto.getDates())
                .dayTime(dto.isDayTime())
                .nightTime(dto.isNightTime())
                .dayAvgTemp(dto.getDayAvgTemp())
                .nightAvgTemp(dto.getNightAvgTemp())
                .build();

        // Join 등을 위한 객체간 명시
        style.setCommunity(community);
        temp.setCommunity(community);
        community.setCommunityStyle(style);
        community.setCommunityTemp(temp);

        // 스타일, 날씨 저장
        communitySexRepository.save(sex);
        communityStyleRepository.save(style);
        communityTempRepository.save(temp);

        communityRepository.save(community);
        // 커뮤니티 저장 후 값 반환
        return community.getId();
    }

    // 단건 조회 및 이미지/스타일 매핑
    public CommunityDTO get(Long id) {
        Community entity = communityRepository.findById(id).orElseThrow();
        CommunityDTO dto = entityToDTO(entity);

        List<CommunityImage> imgs = communityImageRepository.findByCommunity_IdOrderByIsRepDescIdAsc(id); // 이미지 배열에 담기
        if (!imgs.isEmpty()) { // 이미지가 존재할 경우
            dto.setRepImageUrl(imgs.get(0).getFileName()); // 배열 맨 앞 대표이미지 설정
            dto.setExtraImageUrls(imgs.stream()
                    .skip(1)
                    .map(CommunityImage::getFileName)
                    .collect(Collectors.toList()));
        }

        communityStyleRepository.findById(id).ifPresent(style -> {
            dto.setCasual(style.isCasual());
            dto.setStreet(style.isStreet());
            dto.setFormal(style.isFormal());
            dto.setOutdoor(style.isOutdoor());
        });

        communityTempRepository.findById(id).ifPresent(temp -> {
            dto.setDayTime(temp.isDayTime());
            dto.setNightTime(temp.isNightTime());
            dto.setDayAvgTemp(temp.getDayAvgTemp());
            dto.setNightAvgTemp(temp.getNightAvgTemp());
        });
        return dto;
    }

    // 1) 페이징된 DTO 리스트
    public Page<CommunityDTO> getPage(int page) {
        Pageable pageable = PageRequest.of(page - 1, 25, Sort.by("id").descending()); // id 내림차순 25개 게시물
        return communityRepository.list(null, null, null, null, pageable) // 검색없이 전체 조회기능
                .map(this::arrayToDTO); // 프론트 출력
    }

    // 2) 검색 값이 있을 경우
    public Page<CommunityDTO> searchPage(String type, String keyword, int page) {
        Pageable pageable = PageRequest.of(page - 1, 25, Sort.by("id").descending());
        return communityRepository.list(type, keyword, null, null, pageable)
                .map(this::arrayToDTO);
    }

    // 3) 검색 및 스타일 값이 있을 경우
    public Page<CommunityDTO> searchPageRaw(String type, String keyword, List<String> styleNames, int page) {
        Pageable pageable = PageRequest.of(page - 1, 25, Sort.by("id").descending());
        return communityRepository.list(type, keyword, styleNames, null, pageable)
                .map(this::arrayToDTO);
    }

    // 수정 및 이미지 업데이트 기능
    public void modify(CommunityDTO dto, Member currentUser, MultipartFile repImage, List<MultipartFile> extraImages,
            boolean removeRepImage)
            throws IOException {
        Community community = communityRepository.findById(dto.getId()).orElseThrow();
        community.setTitle(dto.getTitle());
        community.setAuthor(currentUser);
        community.setContent(dto.getContent());

        long count = recommendRepository.countByCommunity(community);
        community.setRecommendCount((int) count);
        communityRepository.save(community);

        // ID에 연결된 이미지 가져오기 단 대표는 내림차순 추가 이미지는 등록순서대 정렬
        List<CommunityImage> existing = communityImageRepository.findByCommunity_IdOrderByIsRepDescIdAsc(dto.getId());

        // 대표 이미지 X버튼 눌렀을 때: 기존 대표사진 삭제
        if (removeRepImage) {
            // 리스트에 하나라도 들어있으면 삭제
            if (!existing.isEmpty()) {
                communityImageRepository.deleteAll(existing);
            }
            // NULL유지
            dto.setRepImageUrl(null);
        }
        // X를 누르지 않더라도 새 대표 이미지 업로드시 기존 대표사진 삭제 후 새 파일 저장
        // 이미지가 NULL이 아니고
        else if (repImage != null && !repImage.isEmpty()) {
            if (!existing.isEmpty()) {
                communityImageRepository.deleteAll(existing);
            }
            saveCommunityImage(community, repImage, true);
            List<CommunityImage> imgs = communityImageRepository.findByCommunity_IdOrderByIsRepDescIdAsc(dto.getId());
            if (!imgs.isEmpty()) {
                dto.setRepImageUrl(imgs.get(0).getFileName());
            }
        }
        // 3. 아무것도 안 하면: 기존 대표사진 유지!
        else {
            dto.setRepImageUrl(existing.isEmpty() ? null : existing.get(0).getFileName());
        }

        // 추가 이미지 저장
        if (extraImages != null) {
            for (MultipartFile mf : extraImages) {
                if (!mf.isEmpty()) {
                    saveCommunityImage(community, mf, false);
                }
            }
        }

        CommunitySex sex = communitySexRepository.findById(dto.getId())
                .orElseGet(() -> CommunitySex.builder().build());
        sex.setMale(dto.isMale());
        sex.setFemale(dto.isFemale());
        sex.setCommunity(community);
        community.setCommunitySex(sex);
        communitySexRepository.save(sex);

        CommunityStyle style = communityStyleRepository.findById(dto.getId())
                .orElseGet(() -> CommunityStyle.builder().build());
        style.setCasual(dto.isCasual());
        style.setStreet(dto.isStreet());
        style.setFormal(dto.isFormal());
        style.setOutdoor(dto.isOutdoor());
        style.setCommunity(community);
        community.setCommunityStyle(style);
        communityStyleRepository.save(style);

        communityRepository.save(community);
    }

    // 삭제 기능
    public void remove(Long id) {
        commentRepository.deleteAll(commentRepository.findByPostIdOrderByCreatedDateAsc(id));
        communityTempRepository.deleteById(id);
        communityStyleRepository.deleteById(id);
        communitySexRepository.deleteById(id);
        communityImageRepository.deleteAll(communityImageRepository.findByCommunity_IdOrderByIsRepDescIdAsc(id));
        communityRepository.deleteById(id);
    }

    // 이미지 저장 기능
    private void saveCommunityImage(Community community, MultipartFile file, boolean isRep) throws IOException {
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
        List<CommunityImage> imgs = communityImageRepository.findByCommunity_IdOrderByIsRepDescIdAsc(entity.getId());
        String repImageUrl = (!imgs.isEmpty()) ? imgs.get(0).getFileName() : null;

        CommunitySex sex = communitySexRepository.findById(entity.getId()).orElse(null);
        CommunityStyle style = communityStyleRepository.findById(entity.getId()).orElse(null);

        return CommunityDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .author(entity.getAuthor())
                .content(entity.getContent())
                .recommendCount(entity.getRecommendCount())
                .repImageUrl(repImageUrl)
                .male(sex != null && sex.isMale())
                .female(sex != null && sex.isFemale())
                .casual(style != null && style.isCasual())
                .street(style != null && style.isStreet())
                .formal(style != null && style.isFormal())
                .outdoor(style != null && style.isOutdoor())
                .createdDate(entity.getCreatedDate())
                .upDateTime(entity.getUpDateTime())
                .build();
    }

    /* 쿼리 결과 배열 → DTO 매핑 (스타일 플래그 포함) */
    private CommunityDTO arrayToDTO(Object[] arr) {
        return CommunityDTO.builder()
                .id((Long) arr[0])
                .title((String) arr[1])
                .author((Member) arr[2])
                .recommendCount((Integer) arr[3])
                .repImageUrl((String) arr[4])
                .createdDate((java.time.LocalDateTime) arr[5])
                .casual((Boolean) arr[6])
                .street((Boolean) arr[7])
                .formal((Boolean) arr[8])
                .outdoor((Boolean) arr[9])
                .dayTime((Boolean) arr[10])
                .nightTime((Boolean) arr[11])
                .dayAvgTemp((double) arr[12])
                .nightAvgTemp((double) arr[13])
                .build();
    }

    @Transactional
    public void recommendPost(Long communityId, Member member) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        boolean alreadyRecommended = recommendRepository.existsByMemberAndCommunity(member, community);
        Optional<Recommend> existRec = recommendRepository.findByMemberAndCommunity(member, community);

        if (!alreadyRecommended) {
            Recommend rec = Recommend.builder()
                    .community(community)
                    .member(member)
                    .build();
            recommendRepository.save(rec);

            community.setRecommendCount(community.getRecommendCount() + 1);
        } else {
            recommendRepository.delete(existRec.get());
            community.setRecommendCount(community.getRecommendCount() - 1);
        }
        communityRepository.save(community);
    }

    // 메인 홈페이지에 top 코디들 추가
    public Map<String, List<CommunityDTO>> getPostsByTempAndStyle(int temp, int pageSize) {
        TemperatureRange range = TemperatureRange.fromTemperature(temp);

        // 스타일 레이블 → CommunityStyle 필드명 매핑
        Map<String, String> styleFieldMap = Map.of(
                "CASUAL", "casual",
                "FORMAL", "formal",
                "STREET", "street",
                "OUTDOOR", "outdoor");

        Map<String, List<CommunityDTO>> result = new LinkedHashMap<>();

        styleFieldMap.forEach((label, fieldName) -> {
            Specification<Community> spec = (root, query, cb) -> {
                Join<Community, CommunityStyle> styleJoin = root.join("communityStyle");
                Join<Community, CommunityTemp> tempJoin = root.join("communityTemp");

                // 스타일 일치 조건
                Predicate stylePred = cb.isTrue(styleJoin.get(fieldName));

                // 낮/밤 기온 범위 조건
                Predicate dayPred = cb.and(
                        cb.isTrue(tempJoin.get("dayTime")),
                        cb.between(
                                tempJoin.get("dayAvgTemp"),
                                range.getMinTemp(),
                                range.getMaxTemp()));
                Predicate nightPred = cb.and(
                        cb.isTrue(tempJoin.get("nightTime")),
                        cb.between(
                                tempJoin.get("nightAvgTemp"),
                                range.getMinTemp(),
                                range.getMaxTemp()));
                Predicate tempPred = cb.or(dayPred, nightPred);

                return cb.and(stylePred, tempPred);
            };

            Pageable pg = PageRequest.of(
                    0,
                    pageSize,
                    Sort.by(Sort.Direction.DESC, "recommendCount"));

            List<CommunityDTO> dtos = communityRepository
                    .findAll(spec, pg)
                    .getContent()
                    .stream()
                    .map(this::entityToDTO)
                    .collect(Collectors.toList());

            result.put(label, dtos);
        });

        return result;
    }
}