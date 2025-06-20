package com.example.tempfit.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tempfit.entity.Member;

public interface MemberRepository extends JpaRepository<Member, String> {

    @EntityGraph(attributePaths = {"roleSet"}, type = EntityGraphType.LOAD)
    Member findByEmailAndFromSocial(String email, boolean fromSocial);
}