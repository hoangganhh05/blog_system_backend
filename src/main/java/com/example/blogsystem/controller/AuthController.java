package com.example.blogsystem.controller;

import com.example.blogsystem.config.JwtUtil;
import com.example.blogsystem.dto.AuthResponse;
import com.example.blogsystem.dto.LoginRequest;
import com.example.blogsystem.dto.RegisterRequest;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.UserService;
import com.example.blogsystem.service.PasswordResetMailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;

@RestController
@RequestMapping({"/auth", "/api/auth", "/v1/auth", "/api/v1/auth"})
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetMailService passwordResetMailService;

    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> otpExpiry = new ConcurrentHashMap<>();

    public AuthController(UserService userService, UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, PasswordResetMailService passwordResetMailService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetMailService = passwordResetMailService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            if (request.getUsername() == null || request.getUsername().isBlank()) {
                return ResponseEntity.badRequest().body("Tên đăng nhập không được để trống!");
            }
            if (request.getPassword() == null || request.getPassword().length() < 8) {
                return ResponseEntity.badRequest().body("Mật khẩu phải có ít nhất 8 ký tự!");
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

            ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .maxAge(3600)
                    .path("/")
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Đăng ký thất bại: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
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

            AuthResponse response = new AuthResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getRole()
            );

            ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .maxAge(3600)
                    .path("/")
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            if (email == null || email.isBlank()) {
                email = body.get("username");
            }
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Vui lòng nhập Email hoặc Tên đăng nhập!");
            }

            String normalizedInput = email.trim().toLowerCase();
            User user = userRepository.findByEmail(normalizedInput)
                    .orElseGet(() -> userRepository.findByUsername(normalizedInput).orElse(null));

            // Always return the same response to prevent account enumeration.
            if (user == null) return ResponseEntity.ok(Map.of("message", "Nếu tài khoản tồn tại, hướng dẫn đặt lại mật khẩu sẽ được gửi."));

            String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
            passwordResetMailService.sendResetCode(user.getEmail(), otp);
            otpStore.put(normalizedInput, otp);
            otpExpiry.put(normalizedInput, LocalDateTime.now().plusMinutes(10));

            return ResponseEntity.ok(Map.of(
                    "message", "Mã OTP đã được tạo thành công.",
                    "message", "Nếu tài khoản tồn tại, hướng dẫn đặt lại mật khẩu sẽ được gửi."
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).body("Dịch vụ gửi email đang tạm thời không khả dụng.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(502).body("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            if (email == null || email.isBlank()) {
                email = body.get("username");
            }
            String otp = body.get("otp");
            String newPassword = body.get("newPassword");
            if (newPassword == null || newPassword.isBlank()) {
                newPassword = body.get("password");
            }

            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("Email/Tên đăng nhập không được để trống!");
            }
            if (otp == null || otp.isBlank()) {
                return ResponseEntity.badRequest().body("Mã OTP không được để trống!");
            }
            if (newPassword == null || newPassword.length() < 8) {
                return ResponseEntity.badRequest().body("Mật khẩu mới phải có ít nhất 8 ký tự!");
            }

            String normalizedInput = email.trim().toLowerCase();
            String savedOtp = otpStore.get(normalizedInput);
            LocalDateTime expiry = otpExpiry.get(normalizedInput);

            boolean isValidOtp = savedOtp != null && savedOtp.equals(otp.trim()) && expiry != null && !LocalDateTime.now().isAfter(expiry);

            if (!isValidOtp) {
                return ResponseEntity.status(400).body("Mã OTP không chính xác hoặc đã hết hạn!");
            }

            User user = userRepository.findByEmail(normalizedInput)
                    .orElseGet(() -> userRepository.findByUsername(normalizedInput).orElse(null));

            if (user == null) {
                return ResponseEntity.status(400).body("Tài khoản không tồn tại!");
            }

            user.setPassword(passwordEncoder.encode(newPassword.trim()));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            otpStore.remove(normalizedInput);
            otpExpiry.remove(normalizedInput);

            return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .maxAge(0)
                .path("/")
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(Map.of("message", "Đăng xuất thành công!"));
    }
}
