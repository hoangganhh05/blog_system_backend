package com.example.blogsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private String title;
    private String content;
    private String thumbNail;
    private java.util.List<String> images = new java.util.ArrayList<>();
    private java.util.List<String> imageUrls = new java.util.ArrayList<>();
    private String status;
    private String bgColor;
    private String videoUrl;
    private String mediaType;
    private String sourceLanguage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer viewCount;
    private Long likesCount = 0L;
    private Long commentsCount = 0L;
    private Long sharesCount = 0L;
    private UserPublicDTO user;
    private CategoryDTO category;
    private PostDTO sharedPost;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDTO {
        private Long id;
        private String name;
        private String description;
    }
}
