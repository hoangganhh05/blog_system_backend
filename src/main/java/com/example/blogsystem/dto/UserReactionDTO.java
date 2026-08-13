package com.example.blogsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReactionDTO {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String avatarColor;
    private String type;
}
