package com.postwarfolklore.backend.service;

import com.postwarfolklore.backend.dto.PostDTO;
import com.postwarfolklore.backend.model.Comment;
import com.postwarfolklore.backend.model.Post;
import com.postwarfolklore.backend.repository.CommentRepository;
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

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Page<PostDTO> getAllPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
            .map(post -> toDTO(post));
    }

    public Page<PostDTO> getPostsByType(Post.PostType type, Pageable pageable) {
        return postRepository.findByTypeOrderByCreatedAtDesc(type, pageable)
            .map(this::toDTO);
    }

    public PostDTO getPostById(UUID id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Post not found: " + id));
        return toDTO(post);
    }

    public PostDTO createPost(String title, String content, Post.PostType type,
                               String author, String thumbnailUrl) {
        Post post = Post.builder()
            .title(title)
            .content(content)
            .type(type)
            .author(author)
            .thumbnailUrl(thumbnailUrl)
            .build();
        return toDTO(postRepository.save(post));
    }

    public PostDTO updatePost(UUID id, String title, String content,
                               String author, String thumbnailUrl) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Post not found: " + id));

        if (title != null) post.setTitle(title);
        if (content != null) post.setContent(content);
        if (author != null) post.setAuthor(author);
        if (thumbnailUrl != null) post.setThumbnailUrl(thumbnailUrl);
        return toDTO(postRepository.save(post));
    }

    public void deletePost(UUID id) {
        postRepository.deleteById(id);
    }

    private PostDTO toDTO(Post post) {
        long commentCount = commentRepository.countByPostIdAndStatus(
            post.getId(), Comment.CommentStatus.APPROVED
        );
        return PostDTO.from(post, commentCount);
    }
}
