package com.example.blogsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoundscapeDTO {
    private Long id;
    private String title;
    private String location;
    private String category;
    private String audioUrl;
    private String imageUrl;
    private String description;
    private String creatorName;
    private Long userId;
    private Long likesCount;
    private Long playsCount;
    private Integer durationSeconds;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
