package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.ChatMessage;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.ChatMessageRepository;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.ChatMessageService;
import com.example.blogsystem.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository, UserRepository userRepository, NotificationService notificationService) {
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public ChatMessage sendMessage(Long senderId, Long receiverId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Nội dung tin nhắn không được để trống!");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        ChatMessage message = new ChatMessage();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content.trim());
        message.setRead(false);
        message.setCreatedAt(LocalDateTime.now());

        ChatMessage saved = chatMessageRepository.save(message);

        // Gửi thông báo có tin nhắn mới cho người nhận
        String senderName = sender.getFullName() != null ? sender.getFullName() : sender.getUsername();
        String excerpt = content.length() > 30 ? content.substring(0, 30) + "..." : content;
        notificationService.createNotification(
                receiver,
                sender,
                null,
                senderName + " đã gửi tin nhắn: \"" + excerpt + "\""
        );

        return saved;
    }

    @Override
    public List<ChatMessage> getChatHistory(Long user1, Long user2) {
        // Tự động đánh dấu đã đọc các tin nhắn gửi tới user1 từ user2
        markAsRead(user2, user1);
        return chatMessageRepository.findChatHistory(user1, user2);
    }

    @Override
    public void markAsRead(Long senderId, Long receiverId) {
        List<ChatMessage> unread = chatMessageRepository.findBySenderIdAndReceiverIdAndIsReadFalse(senderId, receiverId);
        if (!unread.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (ChatMessage msg : unread) {
                msg.setRead(true);
                msg.setReadAt(now);
            }
            chatMessageRepository.saveAll(unread);
        }
    }
}
