package com.example.blogsystem.controller;

import com.example.blogsystem.dto.UserReactionDTO;
import com.example.blogsystem.dto.DTOMapper;
import com.example.blogsystem.service.PostLikeService;
import com.example.blogsystem.config.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/posts")
public class PostLikeController {

    private final PostLikeService postLikeService;
    private final CurrentUser currentUser;

    public PostLikeController(PostLikeService postLikeService, CurrentUser currentUser) {
        this.postLikeService = postLikeService;
        this.currentUser = currentUser;
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long postId,
            @RequestParam(required = false, defaultValue = "LIKE") String type) {
        Map<String, Object> response = postLikeService.toggleReaction(currentUser.id(), postId, type);
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
            @PathVariable Long postId) {
        Long userId = currentUser.id();
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

    @GetMapping("/{postId}/likes/list")
    public ResponseEntity<List<UserReactionDTO>> getReactionsList(@PathVariable Long postId) {
        List<Map<String, Object>> reactionsList = postLikeService.getReactionsList(postId);
        List<UserReactionDTO> userReactionDTOs = reactionsList.stream()
                .map(map -> {
                    UserReactionDTO dto = new UserReactionDTO();
                    dto.setId(((Number) map.get("id")).longValue());
                    dto.setType((String) map.get("type"));
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> userMap = (Map<String, Object>) map.get("user");
                    if (userMap != null) {
                        dto.setUserId(((Number) userMap.get("id")).longValue());
                        dto.setUsername((String) userMap.get("username"));
                        dto.setFullName((String) userMap.get("fullName"));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(userReactionDTOs);
    }
}
