package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Post;
import com.example.blogsystem.entity.PostLike;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.PostLikeRepository;
import com.example.blogsystem.repository.PostRepository;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.NotificationService;
import com.example.blogsystem.service.PostLikeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        if (reactionType == null || reactionType.isBlank()) {
            reactionType = "LIKE";
        }
        reactionType = reactionType.toUpperCase();

        Optional<PostLike> existing = postLikeRepository.findByUserIdAndPostId(userId, postId);
        boolean liked = false;
        String userReaction = null;

        if (existing.isPresent()) {
            PostLike postLike = existing.get();
            if (reactionType.equals(postLike.getType())) {
                // Thả cùng loại cảm xúc -> Hủy cảm xúc (Unlike)
                postLikeRepository.delete(postLike);
                liked = false;
                userReaction = null;
            } else {
                // Đổi loại cảm xúc (ví dụ từ LIKE sang LOVE)
                postLike.setType(reactionType);
                postLikeRepository.save(postLike);
                liked = true;
                userReaction = reactionType;
            }
        } else {
            // Chưa tương tác -> Tạo mới cảm xúc
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            PostLike postLike = new PostLike();
            postLike.setUser(user);
            postLike.setPost(post);
            postLike.setType(reactionType);
            postLike.setCreatedAt(LocalDateTime.now());
            postLikeRepository.save(postLike);

            liked = true;
            userReaction = reactionType;

            // Gửi thông báo cho tác giả bài viết
            User author = post.getUser();
            if (author != null && user != null && !author.getId().equals(user.getId())) {
                String senderName = user.getFullName() != null ? user.getFullName() : user.getUsername();
                String reactionLabel = getReactionLabel(reactionType);
                notificationService.createNotification(
                    author,
                    user,
                    post,
                    senderName + " đã " + reactionLabel + " bài viết của bạn: \"" + post.getTitle() + "\""
                );
            }
        }

        Map<String, Object> res = new java.util.HashMap<>();
        res.put("liked", liked);
        res.put("userReaction", userReaction);
        res.put("count", getLikeCount(postId));
        res.put("reactionsSummary", getReactionsSummary(postId));
        return res;
    }

    private String getReactionLabel(String type) {
        switch (type) {
            case "LOVE": return "bày tỏ cảm xúc Yêu thích với";
            case "HAHA": return "bày tỏ cảm xúc Haha với";
            case "WOW": return "bày tỏ cảm xúc Wow với";
            case "SAD": return "bày tỏ cảm xúc Buồn với";
            case "ANGRY": return "bày tỏ cảm xúc Phẫn nộ với";
            default: return "thích";
        }
    }

    @Override
    public long getLikeCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    @Override
    public boolean isLikedByUser(Long userId, Long postId) {
        if (userId == null) return false;
        return postLikeRepository.existsByUserIdAndPostId(userId, postId);
    }

    @Override
    public String getUserReaction(Long userId, Long postId) {
        if (userId == null) return null;
        return postLikeRepository.findByUserIdAndPostId(userId, postId)
                .map(PostLike::getType)
                .orElse(null);
    }

    @Override
    public java.util.Map<String, Long> getReactionsSummary(Long postId) {
        java.util.List<PostLike> likes = postLikeRepository.findByPostId(postId);
        java.util.Map<String, Long> summary = new java.util.HashMap<>();
        for (PostLike l : likes) {
            String type = l.getType() != null ? l.getType() : "LIKE";
            summary.put(type, summary.getOrDefault(type, 0L) + 1);
        }
        return summary;
    }

    @Override
    public List<Map<String, Object>> getReactionsList(Long postId) {
        List<PostLike> likes = postLikeRepository.findByPostId(postId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (PostLike like : likes) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", like.getId());
            item.put("type", like.getType() != null ? like.getType() : "LIKE");

            User user = like.getUser();
            if (user != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("fullName", user.getFullName());
                item.put("user", userMap);
            }

            result.add(item);
        }

        return result;
    }
}
