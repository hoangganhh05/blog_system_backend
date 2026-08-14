package com.example.blogsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoryViewDTO {
    private Long id;
    private Long storyId;
    private UserPublicDTO user;
    private LocalDateTime viewedAt;
    private String reaction;
}
