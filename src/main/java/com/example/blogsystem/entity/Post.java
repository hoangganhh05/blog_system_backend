package com.example.blogsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private String thumbNail;
    private String status;
    private String bgColor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Số lượt xem — tự động +1 mỗi khi GET /posts/{id}
    private Integer viewCount = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"posts", "comments", "password"})
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = true)
    @JsonIgnoreProperties({"posts", "Posts"})
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shared_post_id", nullable = true)
    @JsonIgnoreProperties({"comments", "likes"})
    private Post sharedPost;

    public Post(Long id) {
        this.id = id;
    }
}
