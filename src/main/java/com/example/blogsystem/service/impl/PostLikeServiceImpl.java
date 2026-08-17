package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Post;
import com.example.blogsystem.entity.PostLike;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.PostLikeRepository;
import com.example.blogsystem.repository.PostRepository;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.NotificationService;
import com.example.blogsystem.service.PostLikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    public PostLikeServiceImpl(PostLikeRepository postLikeRepository, UserRepository userRepository, PostRepository postRepository, NotificationService notificationService) {
        this.postLikeRepository = postLikeRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
    }

    @Override
    public boolean toggleLike(Long userId, Long postId) {
        Map<String, Object> result = toggleReaction(userId, postId, "LIKE");
        return (Boolean) result.get("liked");
    }

    @Override
    public Map<String, Object> toggleReaction(Long userId, Long postId, String reactionType) {
        Optional<PostLike> existing = postLikeRepository.findByUserIdAndPostId(userId, postId);
        boolean liked = false;

        if (existing.isPresent()) {
            // Hủy thả tim (Unlike)
            postLikeRepository.delete(existing.get());
            liked = false;
        } else {
            // Thả tim mới (Like / Heart)
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Post not found"));

            PostLike postLike = PostLike.builder()
                    .user(user)
                    .post(post)
                    .createdAt(LocalDateTime.now())
                    .build();
            postLikeRepository.save(postLike);

            liked = true;

            // Gửi thông báo cho tác giả bài viết
            User author = post.getUser();
            if (author != null && user != null && !author.getId().equals(user.getId())) {
                String senderName = user.getFullName() != null ? user.getFullName() : user.getUsername();
                notificationService.createNotification(
                    author,
                    user,
                    post,
                    senderName + " đã thả tim bài viết của bạn: \"" + post.getTitle() + "\""
                );
            }
        }

        long count = getLikeCount(postId);
        Map<String, Object> res = new HashMap<>();
        res.put("liked", liked);
        res.put("userReaction", liked ? "LIKE" : null);
        res.put("count", count);
        res.put("reactionsSummary", Map.of("LIKE", count));
        return res;
    }

    @Override
    public long getLikeCount(Long postId) {
        try {
            if (postId == null) return 0L;
            return postLikeRepository.countByPostId(postId);
        } catch (Exception e) {
            log.error("[getLikeCount] Lỗi đếm like. postId={}", postId, e);
            return 0L;
        }
    }

    @Override
    public boolean isLikedByUser(Long userId, Long postId) {
        try {
            if (userId == null || postId == null) return false;
            return postLikeRepository.existsByUserIdAndPostId(userId, postId);
        } catch (Exception e) {
            log.error("[isLikedByUser] Lỗi kiểm tra like. userId={} postId={}", userId, postId, e);
            return false;
        }
    }

    @Override
    public String getUserReaction(Long userId, Long postId) {
        try {
            if (userId == null || postId == null) return null;
            return postLikeRepository.existsByUserIdAndPostId(userId, postId) ? "LIKE" : null;
        } catch (Exception e) {
            log.error("[getUserReaction] Lỗi lấy reaction. userId={} postId={}", userId, postId, e);
            return null;
        }
    }

    @Override
    public Map<String, Long> getReactionsSummary(Long postId) {
        try {
            long count = getLikeCount(postId);
            return Map.of("LIKE", count);
        } catch (Exception e) {
            log.error("[getReactionsSummary] Lỗi lấy summary. postId={}", postId, e);
            return Map.of("LIKE", 0L);
        }
    }

    @Override
    public List<Map<String, Object>> getReactionsList(Long postId) {
        List<PostLike> likes = postLikeRepository.findByPostId(postId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (PostLike like : likes) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", like.getId());
            item.put("type", "LIKE");
            item.put("createdAt", like.getCreatedAt());

            User user = like.getUser();
            if (user != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("fullName", user.getFullName());
                userMap.put("avatarUrl", user.getAvatarUrl());
                userMap.put("avatarColor", user.getAvatarColor());
                item.put("user", userMap);
            }

            result.add(item);
        }

        return result;
    }
}
