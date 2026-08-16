package com.example.blogsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user1_id", "user2_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user1_id", nullable = false)
    @JsonIgnoreProperties({"posts", "comments", "password"})
    private User user1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user2_id", nullable = false)
    @JsonIgnoreProperties({"posts", "comments", "password"})
    private User user2;

    @Column(name = "theme", length = 50)
    private String theme = "DEFAULT";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.theme == null || this.theme.trim().isEmpty()) {
            this.theme = "DEFAULT";
        }
    }
}
