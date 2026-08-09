package com.example.blogsystem.repository;

import com.example.blogsystem.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    Optional<Bookmark> findByUserIdAndPostId(Long userId, Long postId);
    List<Bookmark> findByUserId(Long userId);
}
