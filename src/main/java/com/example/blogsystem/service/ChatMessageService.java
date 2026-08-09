package com.example.blogsystem.service;

import com.example.blogsystem.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    ChatMessage sendMessage(Long senderId, Long receiverId, String content);
    List<ChatMessage> getChatHistory(Long user1, Long user2);
    void markAsRead(Long senderId, Long receiverId);
}
