package com.example.blogsystem.controller;

import com.example.blogsystem.dto.DTOMapper;
import com.example.blogsystem.dto.FriendshipDTO;
import com.example.blogsystem.dto.UserPublicDTO;
import com.example.blogsystem.entity.Friendship;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.service.FriendshipService;
import com.example.blogsystem.config.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/friends", "/api/v1/friends"})
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final CurrentUser currentUser;

    public FriendshipController(FriendshipService friendshipService, CurrentUser currentUser) {
        this.friendshipService = friendshipService;
        this.currentUser = currentUser;
    }

    // Gửi lời mời kết bạn: POST /friends/request?receiverId=...
    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> sendRequest(
            @RequestParam Long receiverId) {
        try {
            Map<String, Object> result = friendshipService.sendFriendRequest(currentUser.id(), receiverId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    // Chấp nhận lời mời kết bạn: POST /friends/accept?requesterId=...
    @PostMapping("/accept")
    public ResponseEntity<Map<String, Object>> acceptRequest(
            @RequestParam Long requesterId) {
        try {
            Map<String, Object> result = friendshipService.acceptFriendRequest(currentUser.id(), requesterId);
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
            @RequestParam(required = false) Long userId1,
            @RequestParam(required = false) Long userId2,
            @RequestParam(required = false) Long targetUserId) {
        Long u1 = (userId1 != null) ? userId1 : currentUser.id();
        Long u2 = (userId2 != null) ? userId2 : targetUserId;

        if (!currentUser.id().equals(u1) && !currentUser.id().equals(u2)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }
        Map<String, Object> result = friendshipService.removeOrCancelFriendship(u1, u2);
        return ResponseEntity.ok(result);
    }

    // Kiểm tra trạng thái kết bạn giữa 2 người: GET /friends/status?targetUserId=...
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(
            @RequestParam Long targetUserId) {
        String status = friendshipService.getFriendshipStatus(currentUser.id(), targetUserId);
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        return ResponseEntity.ok(result);
    }

    // Lấy danh sách bạn bè đã kết bạn của chính mình: GET /friends/list
    @GetMapping("/list")
    public ResponseEntity<List<UserPublicDTO>> getMyFriendsList() {
        return getFriendsList(currentUser.id());
    }

    // Lấy danh sách bạn bè đã kết bạn: GET /friends/list/{userId}
    @GetMapping({"/list/{userId}", "/{userId}"})
    public ResponseEntity<List<UserPublicDTO>> getFriendsList(@PathVariable Long userId) {
        List<User> friends = friendshipService.getFriendsList(userId);
        List<UserPublicDTO> dtos = friends.stream()
                .map(DTOMapper::toUserPublicDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Lấy danh sách lời mời kết bạn đang chờ của chính mình: GET /friends/pending
    @GetMapping("/pending")
    public ResponseEntity<List<FriendshipDTO>> getMyPendingRequests() {
        return getPendingRequests(currentUser.id());
    }

    // Lấy danh sách lời mời kết bạn đang chờ: GET /friends/pending/{userId}
    @GetMapping("/pending/{userId}")
    public ResponseEntity<List<FriendshipDTO>> getPendingRequests(@PathVariable Long userId) {
        currentUser.requireOwnerOrAdmin(userId);
        List<Friendship> pending = friendshipService.getPendingRequests(userId);
        List<FriendshipDTO> dtos = pending.stream()
                .map(DTOMapper::toFriendshipDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Đếm số bạn bè: GET /friends/count hoặc GET /friends/count/{userId}
    @GetMapping({"/count", "/count/{userId}"})
    public ResponseEntity<Map<String, Object>> getFriendCount(@PathVariable(required = false) Long userId) {
        Long targetId = (userId != null) ? userId : currentUser.id();
        long count = friendshipService.getFriendCount(targetId);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return ResponseEntity.ok(result);
    }
}
