package com.example.blogsystem.controller;

import com.example.blogsystem.entity.User;
import com.example.blogsystem.service.UserService;
import com.example.blogsystem.config.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;

    public UserController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        if (!currentUser.isAdmin()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id,
                           @RequestBody User user) {
        currentUser.requireOwnerOrAdmin(id);
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        currentUser.requireOwnerOrAdmin(id);
        userService.deleteUser(id);
    }

    // Đổi mật khẩu: PUT /users/{id}/change-password
    // Body: { "oldPassword": "...", "newPassword": "..." }
    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            currentUser.requireOwnerOrAdmin(id);
            String oldPassword = body.get("oldPassword");
            String newPassword = body.get("newPassword");
            userService.changePassword(id, oldPassword, newPassword);
            return ResponseEntity.ok("Đổi mật khẩu thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // Lấy thống kê cá nhân: GET /users/{id}/stats
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable Long id) {
        currentUser.requireOwnerOrAdmin(id);
        Map<String, Object> stats = userService.getUserStats(id);
        return ResponseEntity.ok(stats);
    }
}


