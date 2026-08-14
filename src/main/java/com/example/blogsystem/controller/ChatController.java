package com.example.blogsystem.controller;

import com.example.blogsystem.dto.ChatMessageDTO;
import com.example.blogsystem.dto.DTOMapper;
import com.example.blogsystem.entity.ChatMessage;
import com.example.blogsystem.service.ChatMessageService;
import com.example.blogsystem.config.CurrentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/chat", "/api/v1/chat"})
@Slf4j
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final CurrentUser currentUser;

    public ChatController(ChatMessageService chatMessageService, CurrentUser currentUser) {
        this.chatMessageService = chatMessageService;
        this.currentUser = currentUser;
    }

    // Gửi tin nhắn: POST /chat/send?receiverId=...
    @PostMapping("/send")
    public ResponseEntity<ChatMessageDTO> sendMessage(
            @RequestParam Long receiverId,
            @RequestBody Map<String, String> body) {
        try {
            String content = body.get("content");
            ChatMessage message = chatMessageService.sendMessage(currentUser.id(), receiverId, content);
            return ResponseEntity.ok(DTOMapper.toChatMessageDTO(message));
        } catch (Exception e) {
            log.error("Lỗi gửi tin nhắn từ user {} đến {}: ", currentUser.id(), receiverId, e);
            throw e;
        }
    }

    // Lấy lịch sử nhắn tin chuẩn REST: GET /chat/history?withUser=...
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @RequestParam(required = false) Long withUser,
            @RequestParam(required = false) Long user1,
            @RequestParam(required = false) Long user2) {
        try {
            Long callerId = currentUser.id();
            Long targetUser = withUser;
            if (targetUser == null) {
                if (user1 != null && user2 != null) {
                    if (!callerId.equals(user1) && !callerId.equals(user2)) {
                        throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
                    }
                    targetUser = callerId.equals(user1) ? user2 : user1;
                } else {
                    throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Thiếu tham số target user");
                }
            }
            List<ChatMessage> history = chatMessageService.getChatHistory(callerId, targetUser);
            List<ChatMessageDTO> dtos = history.stream()
                    .map(DTOMapper::toChatMessageDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("Lỗi lấy lịch sử chat cho user {}: ", currentUser.id(), e);
            throw e;
        }
    }

    // Đánh dấu đã đọc: POST /chat/read?senderId=...
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
