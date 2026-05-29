package com.postwarfolklore.backend.repository;

import com.postwarfolklore.backend.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    Optional<Location> findBySlug(String slug);
    List<Location> findAllByOrderByNameAsc();
}
