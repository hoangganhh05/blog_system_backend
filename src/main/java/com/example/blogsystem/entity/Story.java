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

    // Đường dẫn ảnh/video (story ảnh/video)
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

    // Thời điểm hết hạn 24h
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Cờ lưu trữ (khi hết hạn chuyển thành true để lưu vào kho lưu trữ cá nhân)
    @Column(name = "is_archived")
    private Boolean isArchived = false;
}
