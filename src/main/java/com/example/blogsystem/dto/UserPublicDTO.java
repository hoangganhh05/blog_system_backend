package com.example.blogsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPublicDTO {
    private Long id;
    private String username;
    private String fullName;
    private String bio;
    private String avatarColor;
    private String avatarUrl;
    private String bannerUrl;
    private String emailPrivacy;
    private String role;
    private LocalDateTime createdAt;
}
