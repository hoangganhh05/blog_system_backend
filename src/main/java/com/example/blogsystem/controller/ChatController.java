package com.example.blogsystem.controller;

import com.example.blogsystem.entity.ChatMessage;
import com.example.blogsystem.service.ChatMessageService;
import com.example.blogsystem.config.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
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
        String content = body.get("content");
        ChatMessage message = chatMessageService.sendMessage(currentUser.id(), receiverId, content);
        return ResponseEntity.ok(message);
    }

    // Lấy lịch sử nhắn tin giữa 2 người: GET /chat/history?user1=...&user2=...
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @RequestParam Long user1,
            @RequestParam Long user2) {
        Long callerId = currentUser.id();
        if (!callerId.equals(user1) && !callerId.equals(user2)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }
        List<ChatMessage> history = chatMessageService.getChatHistory(user1, user2);
        return ResponseEntity.ok(history);
    }

    // Đánh dấu đã đọc: POST /chat/read?senderId=...&receiverId=...
    @PostMapping("/read")
    public ResponseEntity<Void> markAsRead(
            @RequestParam Long senderId) {
        chatMessageService.markAsRead(senderId, currentUser.id());
        return ResponseEntity.ok().build();
    }
}
