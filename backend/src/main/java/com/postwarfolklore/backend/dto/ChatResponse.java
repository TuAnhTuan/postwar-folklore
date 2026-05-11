package com.postwarfolklore.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChatResponse {
    private String reply;        // Câu trả lời hiển thị trong chat
    private UUID postId;         // null nếu không tìm được bài
    private String postTitle;    // null nếu không tìm được bài
    private boolean found;       // true nếu khớp được bài viết
}
