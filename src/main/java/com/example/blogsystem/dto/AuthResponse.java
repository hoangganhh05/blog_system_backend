package com.example.blogsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor // tạo constructor đủ tất cả field
public class AuthResponse {
    private String token;    // JWT token để dùng các request sau
    private Long userId;     // frontend cần biết mình là user nào
    private String username;
    private String fullName;
    private String role;
}