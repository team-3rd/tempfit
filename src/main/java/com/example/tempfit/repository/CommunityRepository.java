package com.example.tempfit.repository;

import com.example.tempfit.entity.Community;
import com.example.tempfit.repository.search.SearchCommunityRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Community 엔티티용 JPA 레포지토리
 * - 기본 CRUD + 커스텀 검색(SearchCommunityRepository) 확장
 */
public interface CommunityRepository
        extends JpaRepository<Community, Long>, SearchCommunityRepository {
    // JpaRepository: 기본 CRUD 제공
    // SearchCommunityRepository: QueryDSL 등 커스텀 검색 확장
}
