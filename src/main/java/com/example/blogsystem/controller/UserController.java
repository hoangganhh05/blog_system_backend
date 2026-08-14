package com.example.blogsystem.controller;

import com.example.blogsystem.dto.UserPublicDTO;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.service.UserService;
import com.example.blogsystem.config.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public org.springframework.data.domain.Page<UserPublicDTO> getUsers(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        String searchKeyword = (query != null && !query.isBlank()) ? query : q;
        int pageSize = Math.min(Math.max(size, 1), 50);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(page, 0), pageSize);

        return userService.searchUsers(searchKeyword, pageable)
                .map(com.example.blogsystem.dto.DTOMapper::toUserPublicDTO);
    }

    @GetMapping("/search")
    public org.springframework.data.domain.Page<UserPublicDTO> searchUsers(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return getUsers(query, q, page, size);
    }

    @GetMapping("/{id}")
    public UserPublicDTO getUserById(@PathVariable Long id) {
        return com.example.blogsystem.dto.DTOMapper.toUserPublicDTO(userService.getUserById(id));
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


