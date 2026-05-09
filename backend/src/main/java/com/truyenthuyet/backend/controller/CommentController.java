package com.truyenthuyet.backend.controller;

import com.truyenthuyet.backend.dto.CommentDTO;
import com.truyenthuyet.backend.dto.CreateCommentRequest;
import com.truyenthuyet.backend.security.FirebasePrincipal;
import com.truyenthuyet.backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentDTO>> getComments(@RequestParam UUID postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    @PostMapping
    public ResponseEntity<CommentDTO> createComment(
        @RequestParam UUID postId,
        @Valid @RequestBody CreateCommentRequest request,
        @AuthenticationPrincipal FirebasePrincipal principal   // null nếu Guest
    ) {
        return ResponseEntity.ok(commentService.createComment(postId, request, principal));
    }

    // Admin: ẩn comment rác
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> hideComment(@PathVariable Long commentId) {
        commentService.hideComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
