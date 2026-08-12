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
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Bước 1: Đọc header Authorization
        String authHeader = request.getHeader("Authorization");

        // Bước 2: Không có token → cho đi tiếp, SecurityConfig xử lý
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bước 3: Tách "Bearer " lấy token thật
        String token = authHeader.substring(7);

        // Bước 4: Kiểm tra token, nếu hết hạn hoặc không hợp lệ -> reject ngay để tránh fallthrough 403 mơ hồ
        if (!jwtUtil.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token không hợp lệ hoặc đã hết hạn\"}");
            return;
        }


        // Bước 5: Đọc thông tin từ token
        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        if (role == null || !(role.equals("USER") || role.equals("ADMIN"))) {
            filterChain.doFilter(request, response);
            return;
        }

        // Bước 6: Báo cho Spring Security biết user đã xác thực
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        String.valueOf(userId),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Bước 7: Cho đi tiếp vào Controller
        filterChain.doFilter(request, response);
    }
}
