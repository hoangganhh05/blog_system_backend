package com.example.blogsystem.controller;

import com.example.blogsystem.config.JwtUtil;
import com.example.blogsystem.dto.AuthResponse;
import com.example.blogsystem.dto.LoginRequest;
import com.example.blogsystem.dto.RegisterRequest;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, UserRepository userRepository, JwtUtil jwtUtil) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            if (request.getUsername() == null || request.getUsername().isBlank()) {
                return ResponseEntity.badRequest().body("Tên đăng nhập không được để trống!");
            }
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body("Mật khẩu phải có ít nhất 6 ký tự!");
            }
            if (userRepository.existsByUsername(request.getUsername().trim())) {
                return ResponseEntity.badRequest().body("Tên đăng nhập '" + request.getUsername() + "' đã tồn tại!");
            }
            if (request.getEmail() != null && !request.getEmail().isBlank() && userRepository.existsByEmail(request.getEmail().trim())) {
                return ResponseEntity.badRequest().body("Email '" + request.getEmail() + "' đã được sử dụng!");
            }

            // Bước 1: Tạo User entity từ data client gửi lên
            User user = new User();
            user.setUsername(request.getUsername().trim());
            user.setPassword(request.getPassword());
            user.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
            user.setFullName(request.getFullName() != null && !request.getFullName().isBlank() ? request.getFullName().trim() : request.getUsername().trim());
            user.setRole("USER");
            user.setCreatedAt(LocalDateTime.now());

            // Bước 2: Lưu vào DB (UserImpl.createUser sẽ hash password)
            User savedUser = userService.createUser(user);

            // Bước 3: Tạo JWT token cho user vừa đăng ký
            String token = jwtUtil.generateToken(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getRole()
            );

            // Bước 4: Trả về token + thông tin
            AuthResponse response = new AuthResponse(
                    token,
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getFullName(),
                    savedUser.getRole()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Đăng ký thất bại: " + e.getMessage());
        }
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
