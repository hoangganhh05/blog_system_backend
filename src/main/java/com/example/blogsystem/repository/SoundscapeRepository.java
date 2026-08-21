package com.example.blogsystem.repository;

import com.example.blogsystem.entity.Soundscape;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SoundscapeRepository extends JpaRepository<Soundscape, Long> {

    Page<Soundscape> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<Soundscape> findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(String category, Pageable pageable);

    @Query("SELECT s FROM Soundscape s WHERE s.isActive = true AND " +
           "(:category IS NULL OR :category = '' OR LOWER(s.category) = LOWER(:category)) AND " +
           "(:location IS NULL OR :location = '' OR LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%')) OR LOWER(s.title) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "ORDER BY s.createdAt DESC")
    Page<Soundscape> searchSoundscapes(
            @Param("category") String category,
            @Param("location") String location,
            Pageable pageable
    );
}
