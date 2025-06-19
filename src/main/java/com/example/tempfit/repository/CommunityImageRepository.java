package com.example.tempfit.repository;

import com.example.tempfit.entity.CommunityImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommunityImageRepository extends JpaRepository<CommunityImage, Long> {

    /**
     * 주어진 게시글 ID에 속한 이미지들을
     * isRep(true인 대표 이미지) 내림차순으로 조회합니다.
     * (같은 isRep 값끼리는 ID 오름차순으로 정렬)
     */
    List<CommunityImage> findByCommunity_IdOrderByIsRepDescIdAsc(Long communityId);
}

