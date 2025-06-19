package com.example.tempfit.service;

import com.example.tempfit.entity.Comment;
import com.example.tempfit.entity.Community;
import com.example.tempfit.repository.CommentRepository;
import com.example.tempfit.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;
    private final CommunityRepository postRepo;

    @Transactional
    public Comment addComment(Long postId, String authorName, String content) {
        Community post = postRepo.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        Comment comment = Comment.builder()
            .post(post)
            .authorName(authorName)
            .content(content)
            .build();
        return commentRepo.save(comment);
    }

    @Transactional(readOnly = true)
    public List<Comment> getComments(Long postId) {
        return commentRepo.findByPostIdOrderByCreatedDateAsc(postId);
    }
}

