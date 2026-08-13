package com.example.blogsystem.service;

import com.example.blogsystem.entity.Post;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Service
public interface PostService {

    Page<Post> getAllPosts(Pageable pageable);

    Page<Post> getPostsByCategory(Long categoryId, Pageable pageable);
    Post getPostById(Long id);

    Post createPost(Post post, Long currentUserId);

    Post updatePost(Long id, Post post);

    void deletePost(Long id);

    Page<Post> searchPosts(String query, Pageable pageable);

    Post incrementViewCount(Long id);
}