package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Category;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.entity.Post;
import com.example.blogsystem.repository.BookmarkRepository;
import com.example.blogsystem.repository.CategoryRepository;
import com.example.blogsystem.repository.CommentRepository;
import com.example.blogsystem.repository.NotificationRepository;
import com.example.blogsystem.repository.PostLikeRepository;
import com.example.blogsystem.repository.PostRepository;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final BookmarkRepository bookmarkRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    public PostServiceImpl(PostRepository postRepository,
                           CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           NotificationRepository notificationRepository,
                           BookmarkRepository bookmarkRepository,
                           PostLikeRepository postLikeRepository,
                           CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public Page<Post> getAllPosts(Pageable pageable) {
        Page<Post> idsPage = postRepository.findAllIds(pageable);
        List<Long> ids = idsPage.getContent().stream().map(Post::getId).toList();
        if (ids.isEmpty()) {
            return idsPage;
        }
        List<Post> postsWithRelations = postRepository.findAllWithRelationsByIds(ids);
        return new org.springframework.data.domain.PageImpl<>(postsWithRelations, pageable, idsPage.getTotalElements());
    }

    @Override
    public Page<Post> getPostsByCategory(Long categoryId, Pageable pageable) {
        Page<Post> idsPage = postRepository.findIdsByCategoryId(categoryId, pageable);
        List<Long> ids = idsPage.getContent().stream().map(Post::getId).toList();
        if (ids.isEmpty()) {
            return idsPage;
        }
        List<Post> postsWithRelations = postRepository.findAllWithRelationsByIds(ids);
        return new org.springframework.data.domain.PageImpl<>(postsWithRelations, pageable, idsPage.getTotalElements());
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
    @Transactional
    public Post createPost(Post post, Long currentUserId) {
        try {
            User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + currentUserId));

            if (post.getCreatedAt() == null) {
                post.setCreatedAt(LocalDateTime.now());
            }
            if (post.getViewCount() == null) {
                post.setViewCount(0);
            }

            if (post.getCategory() != null && post.getCategory().getId() != null) {
                Category cat = categoryRepository.findById(post.getCategory().getId()).orElse(null);
                post.setCategory(cat);
            } else {
                post.setCategory(null);
            }

            // Nếu là bài chia sẻ (repost/quote) — gắn sharedPost để đếm lượt share sau này
            if (post.getSharedPost() != null && post.getSharedPost().getId() != null) {
                Post original = postRepository.findById(post.getSharedPost().getId()).orElse(null);
                if (original != null) {
                    post.setSharedPost(original);
                }
            }

            if (post.getImageUrls() != null && !post.getImageUrls().isEmpty()) {
                if (post.getThumbNail() == null || post.getThumbNail().isBlank()) {
                    post.setThumbNail(post.getImageUrls().get(0));
                }
            } else if (post.getThumbNail() != null && !post.getThumbNail().isBlank()) {
                post.setImageUrls(new java.util.ArrayList<>(java.util.List.of(post.getThumbNail())));
            }

            post.setUser(currentUser);

            return postRepository.save(post);
        } catch (Exception e) {
            log.error("Lỗi tạo bài viết: ", e);
            throw new RuntimeException("Không thể tạo bài viết: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Post updatePost(Long id, Post post) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        if (post.getTitle() != null && !post.getTitle().trim().isEmpty()) {
            existingPost.setTitle(post.getTitle());
        }
        if (post.getContent() != null) {
            existingPost.setContent(post.getContent());
        }
        if (post.getImageUrls() != null && !post.getImageUrls().isEmpty()) {
            existingPost.setImageUrls(post.getImageUrls());
            existingPost.setThumbNail(post.getImageUrls().get(0));
        } else if (post.getThumbNail() != null) {
            existingPost.setThumbNail(post.getThumbNail());
            if (post.getThumbNail().isBlank()) {
                existingPost.setImageUrls(new java.util.ArrayList<>());
            } else {
                existingPost.setImageUrls(new java.util.ArrayList<>(java.util.List.of(post.getThumbNail())));
            }
        }
        if (post.getStatus() != null) {
            existingPost.setStatus(post.getStatus());
        }
        if (post.getBgColor() != null) {
            existingPost.setBgColor(post.getBgColor());
        }
        if (post.getCategory() != null && post.getCategory().getId() != null) {
            Category cat = categoryRepository.findById(post.getCategory().getId()).orElse(null);
            existingPost.setCategory(cat);
        }

        existingPost.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(existingPost);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết với id: " + id));
        
        // 1. Dọn dẹp các ràng buộc khóa ngoại trước khi xóa post
        try {
            notificationRepository.deleteByPostId(id);
        } catch (Exception e) {
            log.warn("Không thể xóa notifications cho post: {}", id, e);
        }
        try {
            bookmarkRepository.deleteByPostId(id);
        } catch (Exception e) {
            log.warn("Không thể xóa bookmarks cho post: {}", id, e);
        }
        try {
            postLikeRepository.deleteByPostId(id);
        } catch (Exception e) {
            log.warn("Không thể xóa likes cho post: {}", id, e);
        }
        try {
            commentRepository.deleteByPostId(id);
        } catch (Exception e) {
            log.warn("Không thể xóa comments cho post: {}", id, e);
        }

        // 2. Gỡ liên kết bài viết được chia sẻ nếu có bài viết khác đang trỏ tới bài này
        try {
            List<Post> reposts = postRepository.findBySharedPostId(id);
            if (reposts != null && !reposts.isEmpty()) {
                for (Post repost : reposts) {
                    repost.setSharedPost(null);
                    postRepository.save(repost);
                }
            }
        } catch (Exception e) {
            log.warn("Không thể gỡ liên kết reposts cho post: {}", id, e);
        }

        postRepository.delete(post);
        log.info("✅ Đã xóa thành công bài viết ID: {}", id);
    }

    @Override
    public Page<Post> searchPosts(String query, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query, pageable);
    }
}
