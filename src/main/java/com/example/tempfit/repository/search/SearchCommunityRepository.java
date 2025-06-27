package com.example.tempfit.repository.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.tempfit.entity.TemperatureRange;

import java.util.List;

/**
 * 커뮤니티 게시글 확장 검색 기능 인터페이스
 */
public interface SearchCommunityRepository {

    /**
     * 검색 조건(type, keyword, styleNames) 및 페이징에 따라 게시글 목록 조회
     *
     * @param type       검색 타입 ("t": 제목, "a": 작성자)
     * @param keyword    검색어
     * @param styleNames 선택된 스타일 이름 목록
     * @param pageable   페이징 정보
     * @return 각 게시글의 Object[] 요약 정보를 담은 Page 객체
     */
    Page<Object[]> list(String type, String keyword, List<String> styleNames, TemperatureRange range, Pageable pageable);

    /**
     * 게시글 id로 단건 상세 조회
     *
     * @param id 게시글 id
     * @return 게시글 정보 (Object[] 형태)
     */
    Object[] getCommunityByBno(Long id);
}
