package com.example.blogsystem.controller;

import com.example.blogsystem.config.JwtUtil;
import com.example.blogsystem.dto.AuthResponse;
import com.example.blogsystem.dto.LoginRequest;
import com.example.blogsystem.dto.RegisterRequest;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController {
    public final UserService userService;
    public final JwtUtil jwtUtil;
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {

        // Bước 1: Tạo User entity từ data client gửi lên
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // UserImpl sẽ tự hash
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole("USER");                    // mặc định role là USER
        user.setCreatedAt(LocalDateTime.now());

        // Bước 2: Lưu vào DB
        // UserImpl.createUser() sẽ hash password trước khi save
        User savedUser = userService.createUser(user);

        // Bước 3: Tạo JWT token cho user vừa đăng ký
        String token = jwtUtil.generateToken(
                savedUser.getId(),       // lấy id sau khi DB tự sinh
                savedUser.getUsername(),
                savedUser.getRole()
        );

        // Bước 4: Trả về token + thông tin (không có password)
        AuthResponse response = new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getFullName(),
                savedUser.getRole()
        );

        return ResponseEntity.ok(response); // HTTP 200 + body
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(
                    request.getUsername(),
                    request.getPassword()
            );
            String token = jwtUtil.generateToken(
                    user.getId(),
                    user.getUsername(),
                    user.getRole()
            );
            return ResponseEntity.ok(new AuthResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getRole()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
