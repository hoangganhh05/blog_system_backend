package com.example.blogsystem.service;

import com.example.blogsystem.entity.Notification;
import com.example.blogsystem.entity.Post;
import com.example.blogsystem.entity.User;

import java.util.List;

public interface NotificationService {
    Notification createNotification(User recipient, User sender, Post post, String message);
    List<Notification> getUserNotifications(Long userId);
    long getUnreadCount(Long userId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
}
