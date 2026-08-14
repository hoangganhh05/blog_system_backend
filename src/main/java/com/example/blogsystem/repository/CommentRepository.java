package com.example.blogsystem.repository;

import com.example.blogsystem.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);

    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user WHERE c.post.id = :postId ORDER BY c.createdAt DESC")
    List<Comment> findByPostIdWithUser(@Param("postId") Long postId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
