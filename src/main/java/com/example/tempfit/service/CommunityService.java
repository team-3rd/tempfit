package com.example.tempfit.service;

import com.example.tempfit.entity.TemperatureRange;
import com.example.tempfit.dto.CommunityDTO;
import com.example.tempfit.dto.SelectedWeatherDTO;
import com.example.tempfit.entity.Board;
import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.CommunityImage;
import com.example.tempfit.entity.CommunitySex;
import com.example.tempfit.entity.CommunityStyle;
import com.example.tempfit.entity.CommunityTemp;
import com.example.tempfit.entity.Member;
import com.example.tempfit.entity.Recommend;
import com.example.tempfit.entity.Sex;
import com.example.tempfit.repository.CommentRepository;
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
                        List<SelectedWeatherDTO> weatherData) throws IOException {
        // board 값 누락 방지
        if (dto.getBoard() == null) {
            throw new IllegalArgumentException("게시글의 board 값이 누락되었습니다.");
        }

        Community community = Community.builder()
                .title(dto.getTitle())
                .author(currentUser)
                .content(dto.getContent())
                .recommendCount(dto.getRecommendCount())
                .board(dto.getBoard()) // 반드시 board 세팅
                .build();
        communityRepository.save(community);

        saveCommunityImage(community, repImage, true);
        if (extraImages != null) {
            for (MultipartFile mf : extraImages) {
                if (!mf.isEmpty()) {
                    saveCommunityImage(community, mf, false);
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

        sex.setCommunity(community);
        community.setCommunitySex(sex);

        List<Integer> tmps = new ArrayList<>();
        List<String> ptys = new ArrayList<>();
        List<String> skys = new ArrayList<>();
        for (int i = 0; i < weatherData.size(); i++) {
            tmps.add((int) weatherData.get(i).getTmp());
            ptys.add(weatherData.get(i).getPty());
            skys.add(weatherData.get(i).getSky());
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
        dto.setAvgTemp((int) ((mins + maxs) / 2));

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

    // 단일 게시글 조회
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
        return dto;
    }

    // 전체 페이징 조회 (기본 리스트)
    public Page<CommunityDTO> getPage(int page) {
        Pageable pageable = PageRequest.of(page - 1, 10,
                Sort.by(Sort.Direction.DESC, "createdDate"));
        return communityRepository.list(null, null, null, null, pageable)
                .map(this::arrayToDTO);
    }

    // 보드 타입별 페이징 조회
    public Page<CommunityDTO> getPageByBoard(Board board, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10,
                Sort.by(Sort.Direction.DESC, "createdDate"));
        return communityRepository.findByBoard(board, pageable)
                .map(this::entityToDTO);
    }

    // 키워드 검색 페이징
    public Page<CommunityDTO> searchPage(String type, String keyword, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10,
                Sort.by(Sort.Direction.DESC, "createdDate"));
        return communityRepository.list(type, keyword, null, null, pageable)
                .map(this::arrayToDTO);
    }

    // 스타일 필터+검색 페이징
    public Page<CommunityDTO> searchPageRaw(String type,
                                            String keyword,
                                            List<String> styleNames,
                                            int page) {
        Pageable pageable = PageRequest.of(page - 1, 10,
                Sort.by(Sort.Direction.DESC, "createdDate"));
        return communityRepository.list(type, keyword, styleNames, null, pageable)
                .map(this::arrayToDTO);
    }

    public void modify(CommunityDTO dto, Member currentUser, MultipartFile repImage, List<MultipartFile> extraImages,
                       boolean removeRepImage, List<SelectedWeatherDTO> weatherData) throws IOException {
        Community community = communityRepository.findById(dto.getId()).orElseThrow();

        community.setTitle(dto.getTitle());
        community.setAuthor(currentUser);
        community.setContent(dto.getContent());
        community.setBoard(dto.getBoard());

        long count = recommendRepository.countByCommunity(community);
        community.setRecommendCount((int) count);
        communityRepository.save(community);

        List<CommunityImage> existing = communityImageRepository
                .findByCommunity_IdOrderByIsRepDescIdAsc(dto.getId());

        if (removeRepImage) {
            if (!existing.isEmpty()) {
                communityImageRepository.deleteAll(existing);
            }
            dto.setRepImageUrl(null);
        } else if (repImage != null && !repImage.isEmpty()) {
            if (!existing.isEmpty()) {
                communityImageRepository.deleteAll(existing);
            }
            saveCommunityImage(community, repImage, true);
            List<CommunityImage> imgs = communityImageRepository
                    .findByCommunity_IdOrderByIsRepDescIdAsc(dto.getId());
            if (!imgs.isEmpty()) {
                dto.setRepImageUrl(imgs.get(0).getFileName());
            }
        } else {
            dto.setRepImageUrl(existing.isEmpty() ? null
                    : existing.get(0).getFileName());
        }

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

        List<Integer> tmps = new ArrayList<>();
        List<String> ptys = new ArrayList<>();
        List<String> skys = new ArrayList<>();
        for (int i = 0; i < weatherData.size(); i++) {
            tmps.add((int) weatherData.get(i).getTmp());
            ptys.add(weatherData.get(i).getPty());
            skys.add(weatherData.get(i).getSky());
        }

        for (int i = 0; i < ptys.size(); i++) {
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
        dto.setAvgTemp((int) ((mins + maxs) / 2));

        CommunityTemp temp = communityTempRepository.findById(dto.getId())
                .orElseGet(() -> CommunityTemp.builder().build());
        temp.setDates(dto.getDates());
        temp.setMinTemp(dto.getMinTemp());
        temp.setMaxTemp(dto.getMaxTemp());
        temp.setAvgTemp(dto.getAvgTemp());
        temp.setSky(dto.getSky());
        temp.setCommunity(community);
        community.setCommunityTemp(temp);
        communityTempRepository.save(temp);

        communityRepository.save(community);
    }

    // 게시글 삭제
    public void remove(Long id) {
        commentRepository.deleteAll(
                commentRepository.findByPostIdOrderByCreatedDateAsc(id));
        communityTempRepository.deleteById(id);
        communityStyleRepository.deleteById(id);
        communitySexRepository.deleteById(id);
        communityImageRepository.deleteAll(
                communityImageRepository.findByCommunity_IdOrderByIsRepDescIdAsc(id));
        communityRepository.deleteById(id);
    }

    // 이미지 파일 저장
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

    // Entity → DTO 변환
    public CommunityDTO entityToDTO(Community entity) {
        List<CommunityImage> imgs = communityImageRepository
                .findByCommunity_IdOrderByIsRepDescIdAsc(entity.getId());
        String repUrl = (!imgs.isEmpty()) ? imgs.get(0).getFileName() : null;

        CommunityDTO dto = CommunityDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .author(entity.getAuthor())
                .board(entity.getBoard())
                .content(entity.getContent())
                .recommendCount(entity.getRecommendCount())
                .repImageUrl(repUrl)
                .male(entity.getCommunitySex() != null && entity.getCommunitySex().isMale())
                .female(entity.getCommunitySex() != null && entity.getCommunitySex().isFemale())
                .casual(entity.getCommunityStyle() != null && entity.getCommunityStyle().isCasual())
                .street(entity.getCommunityStyle() != null && entity.getCommunityStyle().isStreet())
                .formal(entity.getCommunityStyle() != null && entity.getCommunityStyle().isFormal())
                .outdoor(entity.getCommunityStyle() != null && entity.getCommunityStyle().isOutdoor())
                .createdDate(entity.getCreatedDate())
                .upDateTime(entity.getUpDateTime())
                .build();
        return dto;
    }

    // Native Query 결과 배열 → DTO 변환
    private CommunityDTO arrayToDTO(Object[] arr) {
        return CommunityDTO.builder()
                .id((Long) arr[0])
                .title((String) arr[1])
                .author((Member) arr[2])
                .recommendCount((Integer) arr[3])
                .repImageUrl((String) arr[4])
                .createdDate((LocalDateTime) arr[5])
                .casual((Boolean) arr[6])
                .street((Boolean) arr[7])
                .formal((Boolean) arr[8])
                .outdoor((Boolean) arr[9])
                .minTemp((int) arr[10])
                .maxTemp((int) arr[11])
                .avgTemp((int) arr[12])
                .sky((String) arr[13])
                .build();
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

    // BEST LOOKS 기능
    public Map<String, List<CommunityDTO>> getPostsByTempAndStyle(int temp, int pageSize) {
        TemperatureRange range = TemperatureRange.fromTemperature(temp);
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
                Predicate stylePred = cb.isTrue(styleJoin.get(fieldName));
                Predicate tempPred = cb.between(tempJoin.get("avgTemp"), range.getMinTemp(), range.getMaxTemp());
                return cb.and(stylePred, tempPred);
            };

            Pageable pg = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "recommendCount"));
            List<CommunityDTO> dtos = communityRepository.findAll(spec, pg)
                    .getContent()
                    .stream()
                    .map(this::entityToDTO)
                    .collect(Collectors.toList());
            result.put(label, dtos);
        });
        return result;
    }
}
