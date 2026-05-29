package com.postwarfolklore.backend.repository;

import com.postwarfolklore.backend.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Post> findByTypeOrderByCreatedAtDesc(Post.PostType type, Pageable pageable);

    // Filter by location slug (navigate through FK: post.location.slug)
    Page<Post> findByLocation_SlugOrderByCreatedAtDesc(String slug, Pageable pageable);
    Page<Post> findByTypeAndLocation_SlugOrderByCreatedAtDesc(Post.PostType type, String slug, Pageable pageable);
}
