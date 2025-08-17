package com.example.tempfit.dto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private String content;
    private Long postId;
    private LocalDateTime createdDate;
    private String AuthorId;

    public String getDisplayDate() {
        Duration diff = Duration.between(createdDate, LocalDateTime.now());
        if (diff.toMinutes() < 1) return "방금 전";
        if (diff.toHours() < 1) return diff.toMinutes() + "분 전";
        if (diff.toHours() < 24) return diff.toHours() + "시간 전";
        return createdDate.format(DateTimeFormatter.ofPattern("MM-dd"));
    }
}