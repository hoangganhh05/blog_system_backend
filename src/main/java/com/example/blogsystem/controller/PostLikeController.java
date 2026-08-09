package com.example.blogsystem.controller;

import com.example.blogsystem.service.PostLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostLikeController {

    private final PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long postId,
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "LIKE") String type) {
        Map<String, Object> response = postLikeService.toggleReaction(userId, postId, type);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/likes/count")
    public ResponseEntity<Map<String, Object>> getLikeCount(@PathVariable Long postId) {
        long count = postLikeService.getLikeCount(postId);
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        response.put("reactionsSummary", postLikeService.getReactionsSummary(postId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/likes/check")
    public ResponseEntity<Map<String, Object>> checkLiked(
            @PathVariable Long postId,
            @RequestParam Long userId) {
        boolean liked = postLikeService.isLikedByUser(userId, postId);
        String userReaction = postLikeService.getUserReaction(userId, postId);
        long count = postLikeService.getLikeCount(postId);
        Map<String, Long> summary = postLikeService.getReactionsSummary(postId);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("userReaction", userReaction);
        response.put("count", count);
        response.put("reactionsSummary", summary);
        return ResponseEntity.ok(response);
    }
}
