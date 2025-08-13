package com.example.tempfit.service;

import com.example.tempfit.entity.Comment;
import com.example.tempfit.entity.Community;
import com.example.tempfit.entity.Member;
import com.example.tempfit.repository.CommentRepository;
import com.example.tempfit.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;
    private final CommunityRepository postRepo;

    @Transactional
    public Comment addComment(Long postId, Member author, String content) {
        Community post = postRepo.findById(postId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        Comment comment = Comment.builder()
            .post(post)
            .author(author)
            .content(content)
            .build();
        return commentRepo.save(comment);
    }

    @Transactional(readOnly = true)
    public List<Comment> getComments(Long postId) {
        return commentRepo.findByPostIdOrderByCreatedDateAsc(postId);
    }

    // 단건 댓글 수 (리포지토리 변경 없이 리스트 길이로 계산)
    @Transactional(readOnly = true)
    public int getCommentCount(Long postId) {
        return getComments(postId).size();
    }

    // 여러 건 댓글 수 (간단 구현: N번 호출) — 필요 시 집계 쿼리로 최적화 가능
    @Transactional(readOnly = true)
    public Map<Long, Integer> getCommentCounts(List<Long> postIds) {
        Map<Long, Integer> map = new HashMap<>();
        for (Long id : postIds) {
            map.put(id, getCommentCount(id));
        }
        return map;
    }
}
