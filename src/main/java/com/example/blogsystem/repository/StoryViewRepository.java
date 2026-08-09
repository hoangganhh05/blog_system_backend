package com.example.blogsystem.repository;

import com.example.blogsystem.entity.StoryView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryViewRepository extends JpaRepository<StoryView, Long> {
    List<StoryView> findByStoryIdOrderByViewedAtDesc(Long storyId);
    Optional<StoryView> findByStoryIdAndUserId(Long storyId, Long userId);
    boolean existsByStoryIdAndUserId(Long storyId, Long userId);
}
