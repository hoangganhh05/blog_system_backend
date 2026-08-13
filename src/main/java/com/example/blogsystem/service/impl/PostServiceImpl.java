package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Category;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.entity.Post;
import com.example.blogsystem.repository.CategoryRepository;
import com.example.blogsystem.repository.PostRepository;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public PostServiceImpl(PostRepository postRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAllWithRelations(pageable);
    }

    @Override
    public Page<Post> getPostsByCategory(Long categoryId, Pageable pageable) {
        return postRepository.findByCategoryIdWithRelations(categoryId, pageable);
    }

    @Override
    public Post getPostById(Long id) {
        return postRepository.findByIdWithRelations(id)
            .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Override
    public Post incrementViewCount(Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        int currentViews = post.getViewCount() == null ? 0 : post.getViewCount();
        post.setViewCount(currentViews + 1);
        return postRepository.save(post);
    }

    @Override
    public Post createPost(Post post, Long currentUserId) {
        try {
            // Load User from DB to ensure fully loaded entity
            User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + currentUserId));

            if (post.getCreatedAt() == null) {
                post.setCreatedAt(LocalDateTime.now());
            }
            if (post.getViewCount() == null) {
                post.setViewCount(0);
            }

            // Handle null category - set to null instead of throwing exception
            if (post.getCategory() != null && post.getCategory().getId() != null) {
                Category cat = categoryRepository.findById(post.getCategory().getId()).orElse(null);
                post.setCategory(cat);
            } else {
                post.setCategory(null);
            }

            // Set the fully loaded User
            post.setUser(currentUser);

            return postRepository.save(post);
        } catch (Exception e) {
            log.error("Lỗi tạo bài viết: ", e);
            throw new RuntimeException("Không thể tạo bài viết: " + e.getMessage(), e);
        }
    }

    @Override
    public Post updatePost(Long id, Post post) {
        Post existingPost = postRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Post not found"));
        existingPost.setTitle(post.getTitle());
        existingPost.setContent(post.getContent());
        existingPost.setThumbNail(post.getThumbNail());
        existingPost.setStatus(post.getStatus());
        existingPost.setBgColor(post.getBgColor());
        existingPost.setCategory(post.getCategory());

        // Nếu cho phép đổi tác giả thì mới update user
        // existingPost.setUser(post.getUser());

        existingPost.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(existingPost);
    }
    @Override
    public void deletePost(Long id) {
        if(!postRepository.existsById(id)) {
            throw new RuntimeException("Post not found");
        }
        postRepository.deleteById(id);
    }

    @Override
    public Page<Post> searchPosts(String query, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query, pageable);
    }
}

