package com.example.blogsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "post_translations",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "target_language"})
    }
)
public class PostTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "source_language")
    private String sourceLanguage;

    @Column(name = "target_language", nullable = false)
    private String targetLanguage;

    @Column(columnDefinition = "TEXT")
    private String translatedTitle;

    @Column(columnDefinition = "TEXT")
    private String translatedContent;

    @Column(name = "source_content_hash")
    private String sourceContentHash;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
