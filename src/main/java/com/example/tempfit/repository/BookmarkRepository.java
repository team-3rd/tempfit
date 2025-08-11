package com.example.tempfit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tempfit.entity.Bookmark;
import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.Member;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long>  {
    boolean existsByMemberAndCommunity(Member member, Community community);
    
    Optional<Bookmark> findByMemberAndCommunity(Member member, Community community);
    List<Bookmark> findByMember(Member member);
}
