package com.example.blogsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"posts", "comments", "password"})
    private User user;

    // Đường dẫn ảnh (story ảnh)
    @Column(name = "media_url", length = 512)
    private String mediaUrl;

    // Nội dung text (story chữ)
    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    // Màu nền gradient (story chữ)
    @Column(name = "bg_color", length = 128)
    private String bgColor;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
