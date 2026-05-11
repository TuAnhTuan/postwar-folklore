package com.postwarfolklore.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.postwarfolklore.backend.model.Comment;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data @Builder
public class CommentDTO {
    private Long id;
    private String displayName;
    private String content;
    private Instant createdAt;
    @JsonProperty("isAuthenticated")   // giữ đúng tên, không bị Lombok strip "is"
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
