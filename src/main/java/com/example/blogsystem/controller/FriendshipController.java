package com.example.blogsystem.controller;

import com.example.blogsystem.entity.Friendship;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.service.FriendshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    // Gửi lời mời kết bạn: POST /friends/request?senderId=...&receiverId=...
    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> sendRequest(
            @RequestParam Long senderId,
            @RequestParam Long receiverId) {
        try {
            Map<String, Object> result = friendshipService.sendFriendRequest(senderId, receiverId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    // Chấp nhận lời mời kết bạn: POST /friends/accept?currentUserId=...&requesterId=...
    @PostMapping("/accept")
    public ResponseEntity<Map<String, Object>> acceptRequest(
            @RequestParam Long currentUserId,
            @RequestParam Long requesterId) {
        try {
            Map<String, Object> result = friendshipService.acceptFriendRequest(currentUserId, requesterId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    // Hủy kết bạn hoặc rút/từ chối lời mời: POST /friends/remove?userId1=...&userId2=...
    @PostMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFriendship(
            @RequestParam Long userId1,
            @RequestParam Long userId2) {
        Map<String, Object> result = friendshipService.removeOrCancelFriendship(userId1, userId2);
        return ResponseEntity.ok(result);
    }

    // Kiểm tra trạng thái kết bạn giữa 2 người: GET /friends/status?currentUserId=...&targetUserId=...
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(
            @RequestParam Long currentUserId,
            @RequestParam Long targetUserId) {
        String status = friendshipService.getFriendshipStatus(currentUserId, targetUserId);
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        return ResponseEntity.ok(result);
    }

    // Lấy danh sách bạn bè đã kết bạn: GET /friends/list/{userId}
    @GetMapping("/list/{userId}")
    public ResponseEntity<List<User>> getFriendsList(@PathVariable Long userId) {
        List<User> friends = friendshipService.getFriendsList(userId);
        return ResponseEntity.ok(friends);
    }

    // Lấy danh sách lời mời kết bạn đang chờ: GET /friends/pending/{userId}
    @GetMapping("/pending/{userId}")
    public ResponseEntity<List<Friendship>> getPendingRequests(@PathVariable Long userId) {
        List<Friendship> pending = friendshipService.getPendingRequests(userId);
        return ResponseEntity.ok(pending);
    }

    // Đếm số bạn bè: GET /friends/count/{userId}
    @GetMapping("/count/{userId}")
    public ResponseEntity<Map<String, Object>> getFriendCount(@PathVariable Long userId) {
        long count = friendshipService.getFriendCount(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return ResponseEntity.ok(result);
    }
}
