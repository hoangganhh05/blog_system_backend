package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Conversation;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.ConversationRepository;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.ConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final com.example.blogsystem.repository.ChatMessageRepository chatMessageRepository;

    public ConversationServiceImpl(
            ConversationRepository conversationRepository,
            UserRepository userRepository,
            com.example.blogsystem.repository.ChatMessageRepository chatMessageRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    @Transactional
    public Conversation findOrCreate(Long user1Id, Long user2Id) {
        if (user1Id == null || user2Id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User IDs cannot be null");
        }
        if (user1Id.equals(user2Id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot start conversation with yourself");
        }

        return conversationRepository.findBetweenUsers(user1Id, user2Id)
                .orElseGet(() -> {
                    // Order user IDs consistently to avoid duplicate unique constraint conflicts
                    Long firstId = Math.min(user1Id, user2Id);
                    Long secondId = Math.max(user1Id, user2Id);

                    User u1 = userRepository.findById(firstId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User 1 not found"));
                    User u2 = userRepository.findById(secondId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User 2 not found"));

                    Conversation conv = new Conversation();
                    conv.setUser1(u1);
                    conv.setUser2(u2);
                    conv.setTheme("DEFAULT");
                    conv.setUpdatedAt(LocalDateTime.now());
                    return conversationRepository.save(conv);
                });
    }

    @Override
    @Transactional
    public Conversation updateTheme(Long conversationId, String theme, Long requesterId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!conv.getUser1().getId().equals(requesterId) && !conv.getUser2().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant in this conversation");
        }

        String validTheme = (theme != null && !theme.trim().isEmpty()) ? theme.trim().toUpperCase() : "DEFAULT";
        conv.setTheme(validTheme);
        conv.setUpdatedAt(LocalDateTime.now());
        return conversationRepository.save(conv);
    }

    @Override
    @Transactional
    public Conversation updateThemeBetweenUsers(Long user1Id, Long user2Id, String theme, Long requesterId) {
        Conversation conv = findOrCreate(user1Id, user2Id);
        return updateTheme(conv.getId(), theme, requesterId);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<java.util.Map<String, Object>> getRecentConversations(Long userId) {
        if (userId == null) return java.util.Collections.emptyList();

        java.util.List<com.example.blogsystem.entity.ChatMessage> allMessages =
                chatMessageRepository.findAllByUserOrderByCreatedAtDesc(userId);

        java.util.Map<Long, java.util.Map<String, Object>> partnerMap = new java.util.LinkedHashMap<>();

        for (com.example.blogsystem.entity.ChatMessage msg : allMessages) {
            boolean isSender = msg.getSender() != null && msg.getSender().getId().equals(userId);
            User partner = isSender ? msg.getReceiver() : msg.getSender();
            if (partner == null || partner.getId() == null) continue;

            Long partnerId = partner.getId();
            if (!partnerMap.containsKey(partnerId)) {
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("partnerId", partnerId);

                java.util.Map<String, Object> userMap = new java.util.HashMap<>();
                userMap.put("id", partner.getId());
                userMap.put("fullName", partner.getFullName());
                userMap.put("username", partner.getUsername());
                userMap.put("avatarUrl", partner.getAvatarUrl());
                userMap.put("avatarColor", partner.getAvatarColor());
                userMap.put("isOnline", partner.getIsOnline() != null ? partner.getIsOnline() : false);
                userMap.put("lastActiveAt", partner.getLastActiveAt());
                userMap.put("showActiveStatus", partner.getShowActiveStatus() != null ? partner.getShowActiveStatus() : true);

                item.put("user", userMap);
                item.put("lastMessage", msg.getContent());
                item.put("lastMessageTime", msg.getCreatedAt());
                item.put("lastSenderId", msg.getSender() != null ? msg.getSender().getId() : null);
                item.put("unreadCount", 0L);

                partnerMap.put(partnerId, item);
            }

            // Đếm số tin nhắn chưa đọc
            if (!isSender && !msg.isRead()) {
                java.util.Map<String, Object> existing = partnerMap.get(partnerId);
                long currentUnread = (long) existing.get("unreadCount");
                existing.put("unreadCount", currentUnread + 1);
            }
        }

        // Bổ sung thông tin theme và ID của conversation
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (java.util.Map<String, Object> convItem : partnerMap.values()) {
            Long pId = (Long) convItem.get("partnerId");
            java.util.Optional<Conversation> convOpt = conversationRepository.findBetweenUsers(userId, pId);
            if (convOpt.isPresent()) {
                convItem.put("conversationId", convOpt.get().getId());
                convItem.put("theme", convOpt.get().getTheme());
            } else {
                convItem.put("conversationId", null);
                convItem.put("theme", "DEFAULT");
            }
            result.add(convItem);
        }

        return result;
    }
}
