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

    // Lấy danh sách các story được tạo trong khoảng thời gian từ timeLimit đến nay, sắp xếp mới nhất lên đầu
    @Query("SELECT s FROM Story s WHERE s.createdAt >= :timeLimit ORDER BY s.createdAt DESC")
    List<Story> findActiveStories(@Param("timeLimit") LocalDateTime timeLimit);

    // Lấy story của một User cụ thể
    List<Story> findByUserIdOrderByCreatedAtDesc(Long userId);
}
