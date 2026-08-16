package com.example.blogsystem.controller;

import com.example.blogsystem.config.CurrentUser;
import com.example.blogsystem.config.WebSocketCryptoConfig;
import com.example.blogsystem.entity.Conversation;
import com.example.blogsystem.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/conversations", "/api/conversations", "/v1/conversations", "/api/v1/conversations"})
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;
    private final CurrentUser currentUser;
    private final WebSocketCryptoConfig.SecureRealtimeHandler realtimeHandler;

    public ConversationController(
            ConversationService conversationService,
            CurrentUser currentUser,
            @org.springframework.beans.factory.annotation.Autowired(required = false) WebSocketCryptoConfig.SecureRealtimeHandler realtimeHandler) {
        this.conversationService = conversationService;
        this.currentUser = currentUser;
        this.realtimeHandler = realtimeHandler;
    }

    @GetMapping("/with-user/{targetUserId}")
    public ResponseEntity<Map<String, Object>> getConversationWithUser(@PathVariable Long targetUserId) {
        try {
            Long callerId = currentUser.id();
            Conversation conv = conversationService.findOrCreate(callerId, targetUserId);

            Map<String, Object> resp = new HashMap<>();
            resp.put("id", conv.getId());
            resp.put("theme", conv.getTheme());
            resp.put("user1Id", conv.getUser1().getId());
            resp.put("user2Id", conv.getUser2().getId());
            resp.put("updatedAt", conv.getUpdatedAt());

            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Lỗi lấy thông tin hội thoại với user {}: ", targetUserId, e);
            throw e;
        }
    }

    @PutMapping("/{id}/theme")
    public ResponseEntity<Map<String, Object>> updateThemeById(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            Long callerId = currentUser.id();
            String theme = body.get("theme");
            Conversation conv = conversationService.updateTheme(id, theme, callerId);

            // Broadcast sự kiện đổi Theme Real-time qua WebSocket
            if (realtimeHandler != null) {
                try {
                    String wsPayload = String.format(
                            "{\"type\":\"THEME_UPDATED\",\"conversationId\":%d,\"user1Id\":%d,\"user2Id\":%d,\"theme\":\"%s\",\"updatedBy\":%d,\"timestamp\":%d}",
                            conv.getId(), conv.getUser1().getId(), conv.getUser2().getId(), conv.getTheme(), callerId, System.currentTimeMillis()
                    );
                    realtimeHandler.broadcastEncryptedMessage(wsPayload);
                } catch (Exception wsEx) {
                    log.warn("Không thể broadcast theme event qua WebSocket: {}", wsEx.getMessage());
                }
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("id", conv.getId());
            resp.put("theme", conv.getTheme());
            resp.put("user1Id", conv.getUser1().getId());
            resp.put("user2Id", conv.getUser2().getId());
            resp.put("updatedAt", conv.getUpdatedAt());

            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Lỗi cập nhật theme cho conversation {}: ", id, e);
            throw e;
        }
    }

    @PutMapping("/theme-with-user/{targetUserId}")
    public ResponseEntity<Map<String, Object>> updateThemeWithUser(
            @PathVariable Long targetUserId,
            @RequestBody Map<String, String> body) {
        try {
            Long callerId = currentUser.id();
            String theme = body.get("theme");
            Conversation conv = conversationService.updateThemeBetweenUsers(callerId, targetUserId, theme, callerId);

            // Broadcast sự kiện đổi Theme Real-time qua WebSocket
            if (realtimeHandler != null) {
                try {
                    String wsPayload = String.format(
                            "{\"type\":\"THEME_UPDATED\",\"conversationId\":%d,\"user1Id\":%d,\"user2Id\":%d,\"theme\":\"%s\",\"updatedBy\":%d,\"timestamp\":%d}",
                            conv.getId(), conv.getUser1().getId(), conv.getUser2().getId(), conv.getTheme(), callerId, System.currentTimeMillis()
                    );
                    realtimeHandler.broadcastEncryptedMessage(wsPayload);
                } catch (Exception wsEx) {
                    log.warn("Không thể broadcast theme event qua WebSocket: {}", wsEx.getMessage());
                }
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("id", conv.getId());
            resp.put("theme", conv.getTheme());
            resp.put("user1Id", conv.getUser1().getId());
            resp.put("user2Id", conv.getUser2().getId());
            resp.put("updatedAt", conv.getUpdatedAt());

            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Lỗi cập nhật theme với user {}: ", targetUserId, e);
            throw e;
        }
    }
}
