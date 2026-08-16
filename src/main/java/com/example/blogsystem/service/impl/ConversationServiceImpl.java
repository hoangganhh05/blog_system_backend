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

    public ConversationServiceImpl(ConversationRepository conversationRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
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
}
