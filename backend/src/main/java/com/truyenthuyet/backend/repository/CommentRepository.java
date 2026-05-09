package com.truyenthuyet.backend.repository;

import com.truyenthuyet.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdAndStatusOrderByCreatedAtDesc(
        UUID postId, Comment.CommentStatus status
    );

    @Query("SELECT c.postId, COUNT(c) FROM Comment c WHERE c.postId IN :postIds AND c.status = 'APPROVED' GROUP BY c.postId")
    List<Object[]> countApprovedByPostIds(List<UUID> postIds);

    long countByPostIdAndStatus(UUID postId, Comment.CommentStatus status);
}
