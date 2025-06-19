package com.example.tempfit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tempfit.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedDateAsc(Long postId);
}

