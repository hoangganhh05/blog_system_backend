package com.example.blogsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "soundscapes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Soundscape {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 255)
    private String location; // Ví dụ: "Hà Nội", "Sài Gòn", "Đà Lạt", "Cát Tiên"

    @Column(nullable = false, length = 50)
    private String category; // RAIN, CAFE, NATURE, URBAN, OCEAN, SUMMER

    @Column(nullable = false, length = 1000)
    private String audioUrl;

    @Column(length = 1000)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String creatorName; // Tên người thu âm thực tế (Field Recorder)

    private Long userId;

    @Builder.Default
    private Long likesCount = 0L;

    @Builder.Default
    private Long playsCount = 0L;

    private Integer durationSeconds;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
