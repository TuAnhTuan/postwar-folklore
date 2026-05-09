package com.truyenthuyet.backend.dto;

import com.truyenthuyet.backend.model.Comment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class CommentDTO {
    private Long id;
    private String displayName;
    private String content;
    private LocalDateTime createdAt;
    private boolean isAuthenticated;

    public static CommentDTO from(Comment comment) {
        return CommentDTO.builder()
            .id(comment.getId())
            .displayName(comment.getDisplayName())
            .content(comment.getContent())
            .createdAt(comment.getCreatedAt())
            .isAuthenticated(comment.getUserUid() != null)
            .build();
    }
}
