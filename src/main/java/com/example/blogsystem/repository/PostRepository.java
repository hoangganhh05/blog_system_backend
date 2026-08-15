package com.example.blogsystem.repository;

import com.example.blogsystem.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);
    List<Post> findByUserId(Long userId);
    List<Post> findBySharedPostId(Long sharedPostId);
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.category LEFT JOIN FETCH p.sharedPost WHERE p.id = :id")
    Optional<Post> findByIdWithRelations(Long id);

    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    Page<Post> findAllIds(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.category LEFT JOIN FETCH p.sharedPost WHERE p.id IN :ids")
    List<Post> findAllWithRelationsByIds(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId ORDER BY p.createdAt DESC")
    Page<Post> findIdsByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.category LEFT JOIN FETCH p.sharedPost WHERE p.category.id = :categoryId ORDER BY p.createdAt DESC")
    Page<Post> findByCategoryIdWithRelations(Long categoryId, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT p) FROM Post p")
    Long countAllPosts();

    @Query("SELECT COUNT(DISTINCT p) FROM Post p WHERE p.category.id = :categoryId")
    Long countPostsByCategory(Long categoryId);
}
