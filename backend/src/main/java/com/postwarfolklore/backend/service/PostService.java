package com.postwarfolklore.backend.service;

import com.postwarfolklore.backend.dto.PostDTO;
import com.postwarfolklore.backend.model.Comment;
import com.postwarfolklore.backend.model.Location;
import com.postwarfolklore.backend.model.Post;
import com.postwarfolklore.backend.repository.CommentRepository;
import com.postwarfolklore.backend.repository.LocationRepository;
import com.postwarfolklore.backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository     postRepository;
    private final CommentRepository  commentRepository;
    private final LocationRepository locationRepository;

    public Page<PostDTO> getAllPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
            .map(this::toDTO);
    }

    public Page<PostDTO> getPostsByType(Post.PostType type, Pageable pageable) {
        return postRepository.findByTypeOrderByCreatedAtDesc(type, pageable)
            .map(this::toDTO);
    }

    public Page<PostDTO> getPostsByTypeAndLocation(Post.PostType type, String locationSlug, Pageable pageable) {
        return postRepository.findByTypeAndLocation_SlugOrderByCreatedAtDesc(type, locationSlug, pageable)
            .map(this::toDTO);
    }

    public Page<PostDTO> getPostsByLocation(String locationSlug, Pageable pageable) {
        return postRepository.findByLocation_SlugOrderByCreatedAtDesc(locationSlug, pageable)
            .map(this::toDTO);
    }

    public PostDTO getPostById(UUID id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Post not found: " + id));
        return toDTO(post);
    }

    public PostDTO createPost(String title, String content, Post.PostType type,
                               String author, String thumbnailUrl, String locationSlug) {
        Location location = resolveLocation(locationSlug);
        Post post = Post.builder()
            .title(title)
            .content(content)
            .type(type)
            .author(author)
            .thumbnailUrl(thumbnailUrl)
            .location(location)
            .build();
        return toDTO(postRepository.save(post));
    }

    public PostDTO updatePost(UUID id, String title, String content,
                               String author, String thumbnailUrl, String locationSlug) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Post not found: " + id));

        if (title != null)        post.setTitle(title);
        if (content != null)      post.setContent(content);
        if (author != null)       post.setAuthor(author);
        if (thumbnailUrl != null) post.setThumbnailUrl(thumbnailUrl);
        if (locationSlug != null) post.setLocation(resolveLocation(locationSlug));

        return toDTO(postRepository.save(post));
    }

    public void deletePost(UUID id) {
        postRepository.deleteById(id);
    }

    // ── Helpers ──

    private Location resolveLocation(String slug) {
        if (slug == null || slug.isBlank()) return null;
        return locationRepository.findBySlug(slug)
            .orElseThrow(() -> new NoSuchElementException("Location not found: " + slug));
    }

    private PostDTO toDTO(Post post) {
        long commentCount = commentRepository.countByPostIdAndStatus(
            post.getId(), Comment.CommentStatus.APPROVED
        );
        return PostDTO.from(post, commentCount);
    }
}
