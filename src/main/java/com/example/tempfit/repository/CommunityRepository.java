package com.example.tempfit.repository;

import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.Member;
import com.example.tempfit.repository.search.SearchCommunityRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Community 엔티티용 JPA 레포지토리
 * - 기본 CRUD + 커스텀 검색(SearchCommunityRepository) 확장
 */
public interface CommunityRepository
        extends JpaRepository<Community, Long>,
                SearchCommunityRepository,
                CustomCommunityRepository,
                JpaSpecificationExecutor<Community> {
                        List<Community> findByAuthor(Member author);
}
