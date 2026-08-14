package com.example.blogsystem.repository;

import com.example.blogsystem.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    long countByPostId(Long postId);
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    Optional<PostLike> findByUserIdAndPostId(Long userId, Long postId);
    List<PostLike> findByUserId(Long userId);
    
    @Query("SELECT pl FROM PostLike pl JOIN FETCH pl.user u WHERE pl.post.id = :postId")
    List<PostLike> findByPostId(Long postId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PostLike pl WHERE pl.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
