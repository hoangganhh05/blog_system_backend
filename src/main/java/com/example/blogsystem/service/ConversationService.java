package com.example.blogsystem.service;

import com.example.blogsystem.entity.Conversation;

import java.util.List;
import java.util.Map;

public interface ConversationService {
    Conversation findOrCreate(Long user1Id, Long user2Id);
    Conversation updateTheme(Long conversationId, String theme, Long requesterId);
    Conversation updateThemeBetweenUsers(Long user1Id, Long user2Id, String theme, Long requesterId);
    List<Map<String, Object>> getRecentConversations(Long userId);
}
