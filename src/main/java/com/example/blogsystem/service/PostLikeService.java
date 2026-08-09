package com.example.blogsystem.service;

import java.util.Map;

public interface PostLikeService {
    boolean toggleLike(Long userId, Long postId);
    Map<String, Object> toggleReaction(Long userId, Long postId, String reactionType);
    long getLikeCount(Long postId);
    boolean isLikedByUser(Long userId, Long postId);
    String getUserReaction(Long userId, Long postId);
    Map<String, Long> getReactionsSummary(Long postId);
}
