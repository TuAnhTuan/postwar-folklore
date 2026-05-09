package com.truyenthuyet.backend.repository;

import com.truyenthuyet.backend.model.SystemPrompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemPromptRepository extends JpaRepository<SystemPrompt, Long> {
    Optional<SystemPrompt> findFirstByActiveTrue();
}
