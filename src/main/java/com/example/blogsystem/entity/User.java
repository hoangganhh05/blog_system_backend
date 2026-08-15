package com.example.blogsystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    @JsonIgnore
    private String email;
    @JsonIgnore
    private String password;
    private String fullName;

    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thông tin cá nhân bổ sung
    private String bio;         // giới thiệu bản thân
    private String avatarColor; // màu nền avatar (ví dụ: "#1877f2")
    private String avatarUrl;   // link ảnh đại diện
    private String bannerUrl;   // link ảnh bìa
    private String emailPrivacy = "private"; // quyền riêng tư email: "private", "friends", "public"

    // Cài đặt quyền riêng tư bài viết & tương tác lưu Database vĩnh viễn
    private String postVisibility = "PUBLIC"; // "PUBLIC", "FRIENDS", "ONLY_ME"
    private String friendRequestScope = "EVERYONE"; // "EVERYONE", "FRIENDS_OF_FRIENDS"
    private String messageScope = "EVERYONE"; // "EVERYONE", "FRIENDS"
    private Boolean showActiveStatus = true;
    private Boolean showFollowingList = true;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;

    public User(Long id) {
        this.id = id;
    }
}
