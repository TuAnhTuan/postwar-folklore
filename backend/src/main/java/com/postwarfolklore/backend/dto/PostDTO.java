package com.postwarfolklore.backend.dto;

import com.postwarfolklore.backend.model.Post;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class PostDTO {
    private UUID id;
    private Post.PostType type;
    private LocationDTO location;
    private String title;
    private String content;
    private String author;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private long commentCount;

    public static PostDTO from(Post post, long commentCount) {
        return PostDTO.builder()
            .id(post.getId())
            .type(post.getType())
            .location(post.getLocation() != null ? LocationDTO.from(post.getLocation()) : null)
            .title(post.getTitle())
            .content(post.getContent())
            .author(post.getAuthor())
            .thumbnailUrl(post.getThumbnailUrl())
            .createdAt(post.getCreatedAt())
            .commentCount(commentCount)
            .build();
    }
}
