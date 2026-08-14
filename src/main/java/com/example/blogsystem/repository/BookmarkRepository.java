package com.example.blogsystem.repository;

import com.example.blogsystem.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    Optional<Bookmark> findByUserIdAndPostId(Long userId, Long postId);
    
    @Query("SELECT b FROM Bookmark b JOIN FETCH b.user u JOIN FETCH b.post p WHERE b.user.id = :userId")
    List<Bookmark> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Bookmark b WHERE b.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
