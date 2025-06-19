package com.example.tempfit.service;

import com.example.tempfit.dto.CoordiDTO;
import com.example.tempfit.entity.CommunityStyle;
import com.example.tempfit.entity.Coordi;
import com.example.tempfit.entity.TemperatureRange;
import com.example.tempfit.repository.CoordiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoordiService {

    private final CoordiRepository coordiRepository;

    // 게시글 등록
    public Long register(CoordiDTO dto) {
        Coordi coordi = Coordi.builder()
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .imageUrl(dto.getImageUrl())
                .temperatureRange(dto.getTemperatureRange())
                .recommendCount(0)
                .build();

        CommunityStyle flags = CommunityStyle.builder()
                .casual(dto.isCasual())
                .street(dto.isStreet())
                .formal(dto.isFormal())
                .outdoor(dto.isOutdoor())
                .build();

        coordi.setCommunityStyle(flags);
        coordiRepository.save(coordi);
        return coordi.getId();
    }

    // 온도에 맞는 스타일별 추천 상위 5개
    public Map<String, List<CoordiDTO>> getRecommendationsByTemp(int temp) {
        TemperatureRange range = TemperatureRange.fromTemperature(temp);
        Map<String, List<CoordiDTO>> result = new LinkedHashMap<>();

        result.put("casual",
            coordiRepository
                .findTop5ByCommunityStyleCasualTrueAndTemperatureRangeOrderByRecommendCountDesc(range)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList())
        );
        result.put("street",
            coordiRepository
                .findTop5ByCommunityStyleStreetTrueAndTemperatureRangeOrderByRecommendCountDesc(range)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList())
        );
        result.put("formal",
            coordiRepository
                .findTop5ByCommunityStyleFormalTrueAndTemperatureRangeOrderByRecommendCountDesc(range)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList())
        );
        result.put("outdoor",
            coordiRepository
                .findTop5ByCommunityStyleOutdoorTrueAndTemperatureRangeOrderByRecommendCountDesc(range)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList())
        );

        return result;
    }

    // 온도범위별 전체 게시글
    public List<CoordiDTO> getAllByTemperature(int temp) {
        TemperatureRange range = TemperatureRange.fromTemperature(temp);
        return coordiRepository
                .findByTemperatureRangeOrderByRecommendCountDesc(range)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Entity → DTO 변환
    private CoordiDTO toDTO(Coordi entity) {
        CommunityStyle f = entity.getCommunityStyle();
        return CoordiDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .author(entity.getAuthor())
                .imageUrl(entity.getImageUrl())
                .temperatureRange(entity.getTemperatureRange())
                .casual(f.isCasual())
                .street(f.isStreet())
                .formal(f.isFormal())
                .outdoor(f.isOutdoor())
                .recommendCount(entity.getRecommendCount())
                .createdDate(entity.getCreatedDate())
                .build();
    }
}
