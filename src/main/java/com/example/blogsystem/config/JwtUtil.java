package com.example.blogsystem.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component // Spring quản lý class này, có thể inject vào nơi khác
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret; // đọc secret key từ application.properties

    @Value("${jwt.expiration}")
    private long expiration; // đọc thời hạn token từ application.properties (86400000ms = 24h)

    // ==========================================
    // BƯỚC 1: CHUYỂN secret String → SecretKey
    // ==========================================
    private SecretKey getSigningKey() {
        // secret.getBytes() → chuyển chuỗi thành mảng byte vì Keys cần byte[]
        // Keys.hmacShaKeyFor() → tạo SecretKey object từ mảng byte đó
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }

        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        if ("USER".equals(normalized) || "ADMIN".equals(normalized)) {
            return normalized;
        }

        return "USER";
    }

    // ==========================================
    // BƯỚC 2: TẠO TOKEN
    // ==========================================
    public String generateToken(Long userId, String username, String role) {
        String normalizedRole = normalizeRole(role);
        return Jwts.builder()
                // subject = field chuẩn của JWT, thường chứa ID người dùng
                // valueOf() vì subject phải là String, userId là Long
                .subject(String.valueOf(userId))

                // claim() = thêm dữ liệu tùy ý vào payload
                .claim("username", username) // thêm username vào payload
                .claim("role", normalizedRole)         // thêm role vào payload

                // issuedAt = thời điểm tạo token (bây giờ)
                .issuedAt(new Date())

                // expiration = thời điểm hết hạn
                // System.currentTimeMillis() = thời điểm hiện tại (millisecond)
                // + expiration = cộng thêm 24h → token hết hạn sau 24h
                .expiration(new Date(System.currentTimeMillis() + expiration))

                // ký token bằng secret key → tạo ra chữ ký (SIGNATURE)
                .signWith(getSigningKey())

                // ghép HEADER.PAYLOAD.SIGNATURE thành chuỗi hoàn chỉnh
                .compact();
    }

    // ==========================================
    // BƯỚC 3: ĐỌC TOÀN BỘ DỮ LIỆU TRONG TOKEN
    // ==========================================
    private Claims extractAllClaims(String token) {
        return Jwts.parser()            // bắt đầu đọc token
                .verifyWith(getSigningKey()) // dùng cùng secret key để verify chữ ký
                .build()
                .parseSignedClaims(token)   // parse chuỗi token → nếu sai key hoặc hết hạn thì throw Exception
                .getPayload();              // lấy phần payload (chứa data mình đã nhét vào lúc tạo)
    }

    // ==========================================
    // BƯỚC 4: LẤY TỪNG THÔNG TIN CỤ THỂ
    // ==========================================

    // Lấy userId từ token (field "sub")
    public Long extractUserId(String token) {
        String subject = extractAllClaims(token).getSubject(); // lấy "sub" → "1"
        return Long.parseLong(subject); // chuyển "1" → 1L
    }

    // Lấy username từ token (field "username" mình tự thêm vào)
    public String extractUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
        // get("username", String.class) = lấy claim tên "username", kiểu String
    }

    // Lấy role từ token (field "role" mình tự thêm vào)
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // ==========================================
    // BƯỚC 5: KIỂM TRA TOKEN CÒN HỢP LỆ KHÔNG
    // ==========================================
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token); // thử đọc token
            // nếu đọc được → token hợp lệ (đúng chữ ký, chưa hết hạn)
            return true;
        } catch (Exception e) {
            // extractAllClaims throw Exception khi:
            // - Token hết hạn (ExpiredJwtException)
            // - Chữ ký sai / token bị giả mạo (SignatureException)
            // - Token không đúng định dạng (MalformedJwtException)
            return false; // → token không hợp lệ
        }
    }
}