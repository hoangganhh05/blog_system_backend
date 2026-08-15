package com.example.blogsystem.controller;

import com.example.blogsystem.config.CurrentUser;
import com.example.blogsystem.dto.DTOMapper;
import com.example.blogsystem.dto.UserPublicDTO;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/follows", "/api/follows", "/v1/follows", "/api/v1/follows"})
public class FollowController {

    private final FollowService followService;
    private final CurrentUser currentUser;

    public FollowController(FollowService followService, CurrentUser currentUser) {
        this.followService = followService;
        this.currentUser = currentUser;
    }

    // Theo dõi người dùng: POST /follows/{targetUserId} hoặc POST /follows?targetUserId=...
    @PostMapping({"/{targetUserId}", ""})
    public ResponseEntity<Map<String, Object>> followUser(
            @PathVariable(required = false) Long targetUserId,
            @RequestParam(required = false) Long userId) {
        Long target = (targetUserId != null) ? targetUserId : userId;
        if (target == null) {
            throw new RuntimeException("Thiếu targetUserId!");
        }
        Map<String, Object> result = followService.follow(currentUser.id(), target);
        return ResponseEntity.ok(result);
    }

    // Hủy theo dõi người dùng: DELETE /follows/{targetUserId} hoặc POST /follows/unfollow?targetUserId=...
    @DeleteMapping({"/{targetUserId}", ""})
    public ResponseEntity<Map<String, Object>> unfollowUser(
            @PathVariable(required = false) Long targetUserId,
            @RequestParam(required = false) Long userId) {
        Long target = (targetUserId != null) ? targetUserId : userId;
        if (target == null) {
            throw new RuntimeException("Thiếu targetUserId!");
        }
        Map<String, Object> result = followService.unfollow(currentUser.id(), target);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/unfollow")
    public ResponseEntity<Map<String, Object>> unfollowUserPost(@RequestParam Long targetUserId) {
        Map<String, Object> result = followService.unfollow(currentUser.id(), targetUserId);
        return ResponseEntity.ok(result);
    }

    // Kiểm tra trạng thái theo dõi: GET /follows/status?targetUserId=...
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(@RequestParam Long targetUserId) {
        boolean isFollowing = followService.isFollowing(currentUser.id(), targetUserId);
        Map<String, Object> result = new HashMap<>();
        result.put("isFollowing", isFollowing);
        return ResponseEntity.ok(result);
    }

    // Lấy danh sách những người user đang theo dõi: GET /follows/following hoặc GET /follows/following/{userId}
    @GetMapping({"/following", "/following/{userId}"})
    public ResponseEntity<List<UserPublicDTO>> getFollowing(@PathVariable(required = false) Long userId) {
        Long target = (userId != null) ? userId : currentUser.id();
        List<User> followingList = followService.getFollowing(target);
        List<UserPublicDTO> dtos = followingList.stream()
                .map(DTOMapper::toUserPublicDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Lấy danh sách ID những người user đang theo dõi: GET /follows/following-ids
    @GetMapping({"/following-ids", "/following-ids/{userId}"})
    public ResponseEntity<List<Long>> getFollowingIds(@PathVariable(required = false) Long userId) {
        Long target = (userId != null) ? userId : currentUser.id();
        List<Long> ids = followService.getFollowingIds(target);
        return ResponseEntity.ok(ids);
    }

    // Lấy danh sách người theo dõi user: GET /follows/followers hoặc GET /follows/followers/{userId}
    @GetMapping({"/followers", "/followers/{userId}"})
    public ResponseEntity<List<UserPublicDTO>> getFollowers(@PathVariable(required = false) Long userId) {
        Long target = (userId != null) ? userId : currentUser.id();
        List<User> followersList = followService.getFollowers(target);
        List<UserPublicDTO> dtos = followersList.stream()
                .map(DTOMapper::toUserPublicDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Đếm số lượt theo dõi: GET /follows/count hoặc GET /follows/count/{userId}
    @GetMapping({"/count", "/count/{userId}"})
    public ResponseEntity<Map<String, Object>> getFollowCounts(@PathVariable(required = false) Long userId) {
        Long target = (userId != null) ? userId : currentUser.id();
        Map<String, Object> counts = followService.getFollowCounts(target);
        return ResponseEntity.ok(counts);
    }
}
