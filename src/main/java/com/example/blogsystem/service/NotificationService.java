package com.example.blogsystem.service;

import com.example.blogsystem.entity.Notification;
import com.example.blogsystem.entity.Post;
import com.example.blogsystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    Notification createNotification(User recipient, User sender, Post post, String message);
    List<Notification> getUserNotifications(Long userId);
    Page<Notification> getUserNotificationsPaginated(Long userId, Pageable pageable);
    long getUnreadCount(Long userId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
}
