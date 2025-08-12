package com.example.tempfit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tempfit.entity.Bookmark;
import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.Member;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long>  {
    boolean existsByMemberAndCommunity(Member member, Community community);
    
    Optional<Bookmark> findByMemberAndCommunity(Member member, Community community);
    
    @Query("SELECT b.community.id FROM Bookmark b WHERE b.member.email = :email")
    List<Long> findCommunityIdsByMemberEmail(@Param("email") String email);

}
