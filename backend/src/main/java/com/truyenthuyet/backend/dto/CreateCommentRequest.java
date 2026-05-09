package com.truyenthuyet.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {

    // Chỉ bắt buộc nếu là Guest (kiểm tra trong service)
    @Size(max = 100)
    private String displayName;

    @NotBlank
    @Size(max = 2000)
    private String content;
}
