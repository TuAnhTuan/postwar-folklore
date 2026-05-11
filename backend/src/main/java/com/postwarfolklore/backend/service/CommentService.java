package com.postwarfolklore.backend.service;

import com.postwarfolklore.backend.dto.CommentDTO;
import com.postwarfolklore.backend.dto.CreateCommentRequest;
import com.postwarfolklore.backend.model.Comment;
import com.postwarfolklore.backend.repository.CommentRepository;
import com.postwarfolklore.backend.security.FirebasePrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public List<CommentDTO> getCommentsByPostId(UUID postId) {
        return commentRepository
            .findByPostIdAndStatusOrderByCreatedAtDesc(postId, Comment.CommentStatus.APPROVED)
            .stream()
            .map(CommentDTO::from)
            .collect(Collectors.toList());
    }

    public CommentDTO createComment(UUID postId, CreateCommentRequest request,
                                    FirebasePrincipal principal) {
        String userUid = null;
        String displayName;

        if (principal != null) {
            // Authenticated user
            userUid = principal.getUid();
            displayName = principal.getDisplayName();
        } else {
            // Guest — phải có displayName
            if (request.getDisplayName() == null || request.getDisplayName().isBlank()) {
                throw new IllegalArgumentException("Khách phải nhập tên hiển thị.");
            }
            displayName = request.getDisplayName().trim();
        }

        Comment comment = Comment.builder()
            .postId(postId)
            .userUid(userUid)
            .displayName(displayName)
            .content(request.getContent().trim())
            .status(Comment.CommentStatus.APPROVED)
            .build();

        return CommentDTO.from(commentRepository.save(comment));
    }

    public void hideComment(Long commentId) {
        commentRepository.findById(commentId).ifPresent(c -> {
            c.setStatus(Comment.CommentStatus.HIDDEN);
            commentRepository.save(c);
        });
    }
}
