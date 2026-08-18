package com.example.blogsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Boolean isOnline;
    private java.time.LocalDateTime lastActiveAt;
    private Boolean showActiveStatus;

    // Social Media Links
    private String facebookUrl;
    private String tiktokUrl;
    private String instagramUrl;
    private String youtubeUrl;
    private String githubUrl;
    private String twitterUrl;
}
