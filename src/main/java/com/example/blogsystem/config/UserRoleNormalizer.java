package com.example.blogsystem.config;

import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserRoleNormalizer implements CommandLineRunner {

    private final UserRepository userRepository;

    public UserRoleNormalizer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            String normalizedRole = normalizeRole(user.getRole());
            if (!normalizedRole.equals(user.getRole())) {
                user.setRole(normalizedRole);
                userRepository.save(user);
            }
        }
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }

        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        return switch (normalized) {
            case "ADMIN" -> "ADMIN";
            default -> "USER";
        };
    }
}