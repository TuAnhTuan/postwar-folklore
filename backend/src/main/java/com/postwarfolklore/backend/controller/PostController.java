package com.postwarfolklore.backend.controller;

import com.postwarfolklore.backend.dto.PostDTO;
import com.postwarfolklore.backend.model.Post;
import com.postwarfolklore.backend.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // ── PUBLIC ──

    @GetMapping("/posts")
    public ResponseEntity<Page<PostDTO>> getAllPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping("/posts/type/{type}")
    public ResponseEntity<Page<PostDTO>> getPostsByType(
        @PathVariable Post.PostType type,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.getPostsByType(type, pageable));
    }

    // ── ADMIN ──

    @PostMapping("/admin/posts")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<PostDTO> createPost(
        @RequestParam String title,
        @RequestParam String content,
        @RequestParam Post.PostType type,
        @RequestParam(required = false) String author,
        @RequestParam(required = false) String thumbnailUrl
    ) {
        return ResponseEntity.ok(postService.createPost(title, content, type, author, thumbnailUrl));
    }

    @PutMapping("/admin/posts/{id}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<PostDTO> updatePost(
        @PathVariable UUID id,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String content,
        @RequestParam(required = false) String author,
        @RequestParam(required = false) String thumbnailUrl
    ) {
        return ResponseEntity.ok(postService.updatePost(id, title, content, author, thumbnailUrl));
    }

    @DeleteMapping("/admin/posts/{id}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
