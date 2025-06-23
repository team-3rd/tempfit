package com.example.tempfit.repository;

import java.util.List;

import com.example.tempfit.entity.Community;

// 메인 홈페이지에 top 코디들 추가
// 추천수 기준, 스타일별, 온도범위에 맞는 상위 5개 게시글
public interface CustomCommunityRepository {
    List<Community> findTopCommunitiesByStyleAndTemp(String style, int temp, int limit);
}
