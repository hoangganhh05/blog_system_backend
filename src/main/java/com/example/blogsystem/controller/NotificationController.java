package com.example.blogsystem.controller;

import com.example.blogsystem.entity.Notification;
import com.example.blogsystem.service.NotificationService;
import com.example.blogsystem.config.CurrentUser;
import com.example.blogsystem.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final CurrentUser currentUser;

    public NotificationController(NotificationService notificationService, NotificationRepository notificationRepository, CurrentUser currentUser) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications() {
        List<Notification> notifications = notificationService.getUserNotifications(currentUser.id());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        long count = notificationService.getUnreadCount(currentUser.id());
        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        currentUser.requireOwnerOrAdmin(notificationRepository.findById(id).orElseThrow().getUser().getId());
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead(currentUser.id());
        return ResponseEntity.ok().build();
    }
}
