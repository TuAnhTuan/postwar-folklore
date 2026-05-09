package com.truyenthuyet.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.truyenthuyet.backend.dto.PostDTO;
import com.truyenthuyet.backend.model.Comment;
import com.truyenthuyet.backend.model.Post;
import com.truyenthuyet.backend.repository.CommentRepository;
import com.truyenthuyet.backend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final Cloudinary cloudinary;

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
                               String author, MultipartFile thumbnail) {
        String thumbnailUrl = uploadImage(thumbnail);
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
                               String author, MultipartFile thumbnail) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Post not found: " + id));

        if (title != null) post.setTitle(title);
        if (content != null) post.setContent(content);
        if (author != null) post.setAuthor(author);
        if (thumbnail != null && !thumbnail.isEmpty()) {
            post.setThumbnailUrl(uploadImage(thumbnail));
        }
        return toDTO(postRepository.save(post));
    }

    public void deletePost(UUID id) {
        postRepository.deleteById(id);
    }

    // ── HELPERS ──

    private PostDTO toDTO(Post post) {
        long commentCount = commentRepository.countByPostIdAndStatus(
            post.getId(), Comment.CommentStatus.APPROVED
        );
        return PostDTO.from(post, commentCount);
    }

    private String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "truyen-thuyet-hau-chien"));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new RuntimeException("Image upload failed", e);
        }
    }
}
