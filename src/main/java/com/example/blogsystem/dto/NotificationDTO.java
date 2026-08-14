package com.example.blogsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private UserPublicDTO user;
    private UserPublicDTO sender;
    private Long postId;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}
