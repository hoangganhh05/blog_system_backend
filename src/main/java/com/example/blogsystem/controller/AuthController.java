package com.example.blogsystem.controller;

import com.example.blogsystem.config.JwtUtil;
import com.example.blogsystem.dto.AuthResponse;
import com.example.blogsystem.dto.LoginRequest;
import com.example.blogsystem.dto.RegisterRequest;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> otpExpiry = new ConcurrentHashMap<>();

    public AuthController(UserService userService, UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String otp = body.get("otp");

            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Email không được để trống!");
            }

            String normalizedEmail = email.trim().toLowerCase();
            User user = userRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống!"));

            if (otp == null || otp.isBlank()) {
                otp = String.valueOf((int) (Math.random() * 900000 + 100000));
            }

            otpStore.put(normalizedEmail, otp.trim());
            otpExpiry.put(normalizedEmail, LocalDateTime.now().plusMinutes(10));

            return ResponseEntity.ok(Map.of(
                    "message", "Mã OTP đã được tạo thành công.",
                    "email", user.getEmail(),
                    "otp", otp.trim()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String otp = body.get("otp");
            String newPassword = body.get("newPassword");

            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Email không được để trống!");
            }
            if (otp == null || otp.isBlank()) {
                return ResponseEntity.badRequest().body("Mã OTP không được để trống!");
            }
            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body("Mật khẩu mới phải có ít nhất 6 ký tự!");
            }

            String normalizedEmail = email.trim().toLowerCase();
            String savedOtp = otpStore.get(normalizedEmail);
            LocalDateTime expiry = otpExpiry.get(normalizedEmail);

            if (savedOtp == null || expiry == null || LocalDateTime.now().isAfter(expiry)) {
                otpStore.remove(normalizedEmail);
                otpExpiry.remove(normalizedEmail);
                return ResponseEntity.status(400).body("Mã OTP đã hết hạn hoặc không hợp lệ!");
            }

            if (!savedOtp.equals(otp.trim())) {
                return ResponseEntity.status(400).body("Mã OTP không chính xác!");
            }

            User user = userRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống!"));

            user.setPassword(passwordEncoder.encode(newPassword.trim()));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            otpStore.remove(normalizedEmail);
            otpExpiry.remove(normalizedEmail);

            return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
