package com.example.blogsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryDTO {
    private Long id;
    private UserPublicDTO user;
    private String mediaUrl;
    private String textContent;
    private String bgColor;
    private LocalDateTime createdAt;
}
