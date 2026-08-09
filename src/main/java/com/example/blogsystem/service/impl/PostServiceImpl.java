package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Category;

import com.example.blogsystem.entity.Post;
import com.example.blogsystem.repository.CategoryRepository;
import com.example.blogsystem.repository.PostRepository;
import com.example.blogsystem.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public PostServiceImpl(PostRepository postRepository, CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Override
    public Page<Post> getPostsByCategory(Long categoryId, Pageable pageable) {
        return postRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Post getPostById(Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found"));
        // Tăng view count mỗi lần xem chi tiết
        int currentViews = post.getViewCount() == null ? 0 : post.getViewCount();
        post.setViewCount(currentViews + 1);
        return postRepository.save(post);
    }

    @Override
    public Post createPost(Post post) {
        if (post.getCreatedAt() == null) {
            post.setCreatedAt(LocalDateTime.now());
        }
        if (post.getViewCount() == null) {
            post.setViewCount(0);
        }

        // Nếu người dùng không chọn danh mục -> Tự động chọn danh mục đầu tiên hoặc tạo "Chung"
        if (post.getCategory() == null || post.getCategory().getId() == null) {
            List<Category> allCats = categoryRepository.findAll();
            if (!allCats.isEmpty()) {
                post.setCategory(allCats.get(0));
            } else {
                Category defaultCat = new Category();
                defaultCat.setName("Chung");
                defaultCat.setDescription("Danh mục mặc định");
                defaultCat.setCreatedAt(LocalDateTime.now());
                post.setCategory(categoryRepository.save(defaultCat));
            }
        } else {
            Category cat = categoryRepository.findById(post.getCategory().getId()).orElse(null);
            if (cat != null) {
                post.setCategory(cat);
            }
        }

        return postRepository.save(post);
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

