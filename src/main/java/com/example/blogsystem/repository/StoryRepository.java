package com.example.blogsystem.repository;

import com.example.blogsystem.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    // Lấy danh sách các story còn hiệu lực (< 24h và chưa lưu trữ)
    @Query("SELECT s FROM Story s WHERE s.createdAt >= :timeLimit AND (s.isArchived = false OR s.isArchived IS NULL) ORDER BY s.createdAt DESC")
    List<Story> findActiveStories(@Param("timeLimit") LocalDateTime timeLimit);

    // Lấy story của một User cụ thể
    List<Story> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Lấy kho lưu trữ tin (Story Archive) của người dùng
    @Query("SELECT s FROM Story s WHERE s.user.id = :userId AND (s.isArchived = true OR s.createdAt < :timeLimit) ORDER BY s.createdAt DESC")
    List<Story> findArchivedStoriesByUserId(@Param("userId") Long userId, @Param("timeLimit") LocalDateTime timeLimit);
}
