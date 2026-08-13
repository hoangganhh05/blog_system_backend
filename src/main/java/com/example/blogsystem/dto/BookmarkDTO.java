package com.example.blogsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkDTO {
    private Long id;
    private UserPublicDTO user;
    private PostDTO post;
    private LocalDateTime createdAt;
}
