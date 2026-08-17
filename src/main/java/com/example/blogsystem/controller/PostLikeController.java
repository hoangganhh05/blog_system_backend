package com.example.blogsystem.controller;

import com.example.blogsystem.dto.UserReactionDTO;
import com.example.blogsystem.service.PostLikeService;
import com.example.blogsystem.config.CurrentUser;
import com.example.blogsystem.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping({"/posts", "/api/posts", "/v1/posts", "/api/v1/posts"})
public class PostLikeController {

    private final PostLikeService postLikeService;
    private final PostRepository postRepository;
    private final CurrentUser currentUser;

    public PostLikeController(PostLikeService postLikeService, PostRepository postRepository, CurrentUser currentUser) {
        this.postLikeService = postLikeService;
        this.postRepository = postRepository;
        this.currentUser = currentUser;
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long postId,
            @RequestParam(required = false, defaultValue = "LIKE") String type) {
        if (postId == null || !postRepository.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bài viết không tồn tại");
        }
        Map<String, Object> response = postLikeService.toggleReaction(currentUser.id(), postId, type);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/likes/count")
    public ResponseEntity<Map<String, Object>> getLikeCount(@PathVariable Long postId) {
        if (postId == null || !postRepository.existsById(postId)) {
            Map<String, Object> response = new HashMap<>();
            response.put("count", 0L);
            response.put("reactionsSummary", Map.of("LIKE", 0L));
            return ResponseEntity.ok(response);
        }
        long count = postLikeService.getLikeCount(postId);
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        response.put("reactionsSummary", postLikeService.getReactionsSummary(postId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/likes/check")
    public ResponseEntity<Map<String, Object>> checkLiked(
            @PathVariable Long postId) {
        // Response an toàn mặc định — luôn trả về 200, không bắn 500
        Map<String, Object> safeDefault = new HashMap<>();
        safeDefault.put("liked", false);
        safeDefault.put("isLiked", false);
        safeDefault.put("userReaction", null);
        safeDefault.put("count", 0L);
        safeDefault.put("reactionsSummary", Map.of("LIKE", 0L));

        if (postId == null || !postRepository.existsById(postId)) {
            return ResponseEntity.ok(safeDefault);
        }

        try {
            Long userId = currentUser.idOrNull(); // null nếu User chưa đăng nhập -> liked=false
            boolean liked = userId != null && postLikeService.isLikedByUser(userId, postId);
            String userReaction = userId != null ? postLikeService.getUserReaction(userId, postId) : null;
            long count = postLikeService.getLikeCount(postId);
            Map<String, Long> summary = postLikeService.getReactionsSummary(postId);

            Map<String, Object> response = new HashMap<>();
            response.put("liked", liked);
            response.put("isLiked", liked);
            response.put("userReaction", userReaction);
            response.put("count", count);
            response.put("reactionsSummary", summary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[checkLiked] Lỗi khi kiểm tra like của user. postId={}", postId, e);
            return ResponseEntity.ok(safeDefault);
        }
    }

    @GetMapping("/{postId}/likes/list")
    public ResponseEntity<List<UserReactionDTO>> getReactionsList(@PathVariable Long postId) {
        if (postId == null || !postRepository.existsById(postId)) {
            return ResponseEntity.ok(List.of());
        }
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
