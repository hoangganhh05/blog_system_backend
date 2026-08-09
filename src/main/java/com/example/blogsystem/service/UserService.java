package com.example.blogsystem.service;

import com.example.blogsystem.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface UserService {

    List<User> getAllUsers();

    User getUserById(Long id);

    User createUser(User user);

    User updateUser(Long id, User user);

    void deleteUser(Long id);
    User login(String username, String rawPassword);
    User changePassword(Long id, String oldPassword, String newPassword);
    java.util.Map<String, Object> getUserStats(Long userId);
}