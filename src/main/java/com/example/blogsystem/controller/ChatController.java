package com.example.blogsystem.controller;

import com.example.blogsystem.entity.ChatMessage;
import com.example.blogsystem.service.ChatMessageService;
import com.example.blogsystem.config.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@Slf4j
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final CurrentUser currentUser;

    public ChatController(ChatMessageService chatMessageService, CurrentUser currentUser) {
        this.chatMessageService = chatMessageService;
        this.currentUser = currentUser;
    }

    // Gửi tin nhắn: POST /chat/send?senderId=...&receiverId=...&content=...
    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(
            @RequestParam Long receiverId,
            @RequestBody Map<String, String> body) {
        try {
            String content = body.get("content");
            ChatMessage message = chatMessageService.sendMessage(currentUser.id(), receiverId, content);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("Lỗi gửi tin nhắn từ user {} đến {}: ", currentUser.id(), receiverId, e);
            throw e;
        }
    }

    // Lấy lịch sử nhắn tin giữa 2 người: GET /chat/history?user1=...&user2=...
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @RequestParam Long user1,
            @RequestParam Long user2) {
        try {
            Long callerId = currentUser.id();
            if (!callerId.equals(user1) && !callerId.equals(user2)) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
            }
            List<ChatMessage> history = chatMessageService.getChatHistory(user1, user2);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Lỗi lấy lịch sử chat giữa user {} và {}: ", user1, user2, e);
            throw e;
        }
    }

    // Đánh dấu đã đọc: POST /chat/read?senderId=...&receiverId=...
    @PostMapping("/read")
    public ResponseEntity<Void> markAsRead(
            @RequestParam Long senderId) {
        try {
            chatMessageService.markAsRead(senderId, currentUser.id());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Lỗi đánh dấu đã đọc tin nhắn từ user {}: ", senderId, e);
            throw e;
        }
    }
}
