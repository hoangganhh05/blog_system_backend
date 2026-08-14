package com.example.blogsystem.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    private boolean isPublicPath(String path, String method) {
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        if (path.startsWith("/auth/") || path.startsWith("/api/auth/")) {
            return true;
        }
        if ((path.contains("/posts/") && path.endsWith("/view")) || (path.contains("/stories/") && path.endsWith("/view"))) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method)) {
            if (path.startsWith("/uploads/") || path.startsWith("/api/uploads/") ||
                path.startsWith("/posts") || path.startsWith("/api/posts") ||
                path.startsWith("/comments") || path.startsWith("/api/comments") ||
                path.startsWith("/categories") || path.startsWith("/api/categories") ||
                path.startsWith("/stories") || path.startsWith("/api/stories") ||
                path.startsWith("/users") || path.startsWith("/api/users") ||
                path.startsWith("/games/caro") || path.startsWith("/api/games/caro")) {
                return true;
            }
        }
        return false;
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String json = String.format(
            "{\"status\":401,\"message\":\"%s\",\"timestamp\":\"%s\"}",
            message, now
        );
        response.getWriter().write(json);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");

        boolean publicRequest = isPublicPath(path, method);

        // Bước 1: Không có token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (!publicRequest) {
                // Ngắt ngay lập tức đối với API cần quyền nếu thiếu Token, không truy vấn DB
                writeUnauthorizedResponse(response, "Yêu cầu xác thực tài khoản (Token không được cung cấp)");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        // Bước 2: Có header Authorization -> Tách lấy token
        String token = authHeader.substring(7).trim();

        // Bước 3: Kiểm tra token, nếu hết hạn hoặc không hợp lệ -> ngắt ngay lập tức (401)
        if (!jwtUtil.isTokenValid(token)) {
            writeUnauthorizedResponse(response, "Phiên làm việc đã hết hạn hoặc Token không hợp lệ");
            return;
        }

        // Bước 4: Đọc thông tin từ token hợp lệ
        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.normalizeRole(jwtUtil.extractRole(token));
        if (role == null) {
            if (!publicRequest) {
                writeUnauthorizedResponse(response, "Token không chứa quyền hợp lệ");
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        // Bước 5: Báo cho Spring Security biết user đã được xác thực
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        String.valueOf(userId),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Cho đi tiếp vào Controller
        filterChain.doFilter(request, response);
    }
}
