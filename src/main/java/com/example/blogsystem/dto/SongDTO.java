package com.example.blogsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String src;
    private String fallbackSrc;
    private Integer durationSeconds;
}
