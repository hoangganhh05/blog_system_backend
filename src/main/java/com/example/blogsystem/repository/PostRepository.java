package com.example.blogsystem.repository;

import com.example.blogsystem.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);
    List<Post> findByUserId(Long userId);
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content, Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.category LEFT JOIN FETCH p.sharedPost WHERE p.id = :id")
    Optional<Post> findByIdWithRelations(Long id);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.category LEFT JOIN FETCH p.sharedPost")
    Page<Post> findAllWithRelations(Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.category LEFT JOIN FETCH p.sharedPost WHERE p.category.id = :categoryId")
    Page<Post> findByCategoryIdWithRelations(Long categoryId, Pageable pageable);
}
