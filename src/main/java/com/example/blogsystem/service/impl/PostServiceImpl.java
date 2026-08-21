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
    private final com.example.blogsystem.service.TranslationService translationService;
    private final com.example.blogsystem.service.R2StorageService r2StorageService;
    private final com.example.blogsystem.repository.PostTranslationRepository postTranslationRepository;

    public PostServiceImpl(PostRepository postRepository,
                           CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           NotificationRepository notificationRepository,
                           BookmarkRepository bookmarkRepository,
                           PostLikeRepository postLikeRepository,
                           CommentRepository commentRepository,
                           com.example.blogsystem.service.TranslationService translationService,
                           com.example.blogsystem.service.R2StorageService r2StorageService,
                           com.example.blogsystem.repository.PostTranslationRepository postTranslationRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.translationService = translationService;
        this.r2StorageService = r2StorageService;
        this.postTranslationRepository = postTranslationRepository;
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

            // Detect source language for the post
            try {
                String textToDetect = (post.getTitle() != null ? post.getTitle() + " " : "") + (post.getContent() != null ? post.getContent() : "");
                String detectedLang = translationService.detectLanguage(textToDetect);
                post.setSourceLanguage(detectedLang);
            } catch (Exception langEx) {
                log.warn("Language detection failed during post creation: {}", langEx.getMessage());
                post.setSourceLanguage("vi");
            }

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
        if (post.getVideoUrl() != null) {
            existingPost.setVideoUrl(post.getVideoUrl());
        }
        if (post.getMediaType() != null) {
            existingPost.setMediaType(post.getMediaType());
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

        // 1. Thu thập tất cả URL media thuộc về bài viết trước khi xóa record
        java.util.Set<String> mediaUrls = new java.util.HashSet<>();
        if (post.getVideoUrl() != null && !post.getVideoUrl().isBlank()) {
            mediaUrls.add(post.getVideoUrl());
        }
        if (post.getThumbNail() != null && !post.getThumbNail().isBlank()) {
            mediaUrls.add(post.getThumbNail());
        }
        if (post.getImageUrls() != null && !post.getImageUrls().isEmpty()) {
            for (String imgUrl : post.getImageUrls()) {
                if (imgUrl != null && !imgUrl.isBlank()) {
                    mediaUrls.add(imgUrl);
                }
            }
        }

        // 2. Xóa các tệp tin media tương ứng khỏi Cloudflare R2
        for (String url : mediaUrls) {
            try {
                r2StorageService.deleteFileByUrl(url);
            } catch (Exception e) {
                log.warn("Không thể xóa media URL [{}] khỏi Cloudflare R2 cho post ID {}: {}", url, id, e.getMessage());
            }
        }
        
        // 3. Dọn dẹp các bảng quan hệ phụ phụ thuộc khóa ngoại trước khi xóa post
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
        try {
            postTranslationRepository.deleteByPostId(id);
        } catch (Exception e) {
            log.warn("Không thể xóa bản dịch cho post: {}", id, e);
        }

        // 4. Gỡ liên kết bài viết được chia sẻ nếu có bài viết khác đang trỏ tới bài này
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

        // 5. Xóa bài viết khỏi Database
        postRepository.delete(post);
        log.info("✅ Đã xóa thành công bài viết ID: {} cùng tất cả tệp media trên Cloudflare R2", id);
    }

    @Override
    public Page<Post> searchPosts(String query, Pageable pageable) {
        return postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query, pageable);
    }

    @Override
    public Page<Post> getRecommendedShortsFeed(int page, int size, List<Long> excludeIds, Long currentUserId) {
        List<Post> allVideos = postRepository.findAllVideoPostsWithRelations();
        if (allVideos == null || allVideos.isEmpty()) {
            return Page.empty();
        }

        // Lọc bỏ những video đã xem trong phiên nếu vẫn còn video ứng viên chưa xem
        List<Post> candidateList = allVideos;
        if (excludeIds != null && !excludeIds.isEmpty()) {
            java.util.Set<Long> excludeSet = new java.util.HashSet<>(excludeIds);
            List<Post> filtered = allVideos.stream()
                    .filter(p -> p != null && p.getId() != null && !excludeSet.contains(p.getId()))
                    .toList();
            if (!filtered.isEmpty()) {
                candidateList = filtered;
            }
        }

        LocalDateTime now = LocalDateTime.now();
        java.util.Random random = new java.util.Random();

        // Xếp hạng đề xuất theo Engagement Score + Time Decay + Exploration
        List<Post> ranked = candidateList.stream().sorted((p1, p2) -> {
            double s1 = computeRecommendationScore(p1, now, random);
            double s2 = computeRecommendationScore(p2, now, random);
            return Double.compare(s2, s1); // Điểm cao nhất lên trước
        }).toList();

        int start = Math.min(page * size, ranked.size());
        int end = Math.min(start + size, ranked.size());
        List<Post> pageContent = ranked.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(
                pageContent,
                org.springframework.data.domain.PageRequest.of(page, size),
                ranked.size()
        );
    }

    private double computeRecommendationScore(Post post, LocalDateTime now, java.util.Random random) {
        if (post == null || post.getId() == null) return 0.0;
        long likes = 0;
        try { likes = postLikeRepository.countByPostId(post.getId()); } catch (Exception ignored) {}
        long comments = 0;
        try { comments = commentRepository.countByPostId(post.getId()); } catch (Exception ignored) {}
        long shares = 0;
        try { shares = postRepository.countBySharedPostId(post.getId()); } catch (Exception ignored) {}
        long views = post.getViewCount() != null ? post.getViewCount() : 0;

        long ageHours = 0;
        if (post.getCreatedAt() != null) {
            ageHours = Math.max(0, java.time.Duration.between(post.getCreatedAt(), now).toHours());
        }
        // HackerNews/TikTok Gravity Time Decay Formula
        double timeDecay = 1.0 / Math.pow(ageHours + 2.0, 1.15);

        // Engagement Score: Trọng số tương tác
        double engagement = (likes * 3.0) + (comments * 4.0) + (shares * 5.0) + (views * 0.5) + 1.0;

        // Exploration Factor: Dao động ngẫu nhiên 20% giúp phân phối video mới
        double exploration = 0.85 + (random.nextDouble() * 0.30);

        return engagement * timeDecay * exploration;
    }
}
