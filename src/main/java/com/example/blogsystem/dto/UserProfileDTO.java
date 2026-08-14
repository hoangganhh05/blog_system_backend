package com.example.blogsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String bio;
    private String avatarColor;
    private String avatarUrl;
    private String bannerUrl;
    private String emailPrivacy;
}
