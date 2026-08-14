package com.example.blogsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private UserPublicDTO sender;
    private UserPublicDTO receiver;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
}
