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
    private String status;
    private String bgColor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer viewCount;
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
