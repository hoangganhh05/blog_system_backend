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
public class SongDTO {
    private Long id;
    private String title;
    private String artist;
    private String genre;
    private String genreColor;
    
    private String cover;
    private String coverUrl;
    
    private String src;
    private String audioUrl;
    
    private String fallbackSrc;
    private String fallbackAudioUrl;
    
    private Integer durationSeconds;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
