package com.example.blogsystem.config;

import com.example.blogsystem.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UserRepository userRepository;

    public SecurityConfig(JwtFilter jwtFilter, UserRepository userRepository) {
        this.jwtFilter = jwtFilter;
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .map(user -> User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRole() != null && user.getRole().startsWith("ROLE_")
                                ? user.getRole().substring(5)
                                : (user.getRole() != null ? user.getRole() : "USER"))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())

                // STATELESS = không dùng Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // Cho phép Preflight OPTIONS request từ CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public Auth endpoints
                        .requestMatchers("/auth/**", "/api/auth/**").permitAll()

                        // File Upload static resources
                        .requestMatchers(HttpMethod.GET, "/uploads/**", "/api/uploads/**").permitAll()

                        // Public GET endpoints
                        .requestMatchers(HttpMethod.GET, "/posts/**", "/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/comments/**", "/api/comments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories/**", "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/stories/**", "/api/stories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/users/**", "/api/users/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/games/caro/**", "/api/games/caro/**").permitAll()

                        // Public view counters
                        .requestMatchers(HttpMethod.POST, "/posts/*/view", "/api/posts/*/view").permitAll()
                        .requestMatchers(HttpMethod.POST, "/stories/*/view", "/api/stories/*/view").permitAll()

                        // Yêu cầu xác thực với tất cả các endpoint khác (bao gồm cả Swagger/Actuator/H2-console nếu bật)
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            response.getWriter().write(String.format(
                                    "{\"status\":401,\"message\":\"Yêu cầu xác thực tài khoản\",\"timestamp\":\"%s\"}", now
                            ));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            response.getWriter().write(String.format(
                                    "{\"status\":403,\"message\":\"Bạn không có quyền thực hiện thao tác này\",\"timestamp\":\"%s\"}", now
                            ));
                        })
                )

                // Đăng ký JwtFilter chạy trước filter mặc định
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
