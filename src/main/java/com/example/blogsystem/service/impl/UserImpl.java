package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Post;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.PostLikeRepository;
import com.example.blogsystem.repository.PostRepository;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    public UserImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, PostRepository postRepository, PostLikeRepository postLikeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public org.springframework.data.domain.Page<User> searchUsers(String query, org.springframework.data.domain.Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        String trimmed = query.trim();
        return userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(trimmed, trimmed, pageable);
    }
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }
    @Override
    public User createUser(User user) {
        String hash = passwordEncoder.encode(user.getPassword());
        user.setPassword(hash);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    @Override
    public User updateUser(Long id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getFullName() != null) existingUser.setFullName(user.getFullName());
        if (user.getEmail() != null) existingUser.setEmail(user.getEmail());

        // Cập nhật thông tin cá nhân mới
        if (user.getBio() != null) existingUser.setBio(user.getBio());
        if (user.getAvatarColor() != null) existingUser.setAvatarColor(user.getAvatarColor());
        if (user.getEmailPrivacy() != null) existingUser.setEmailPrivacy(user.getEmailPrivacy());
        if (user.getPostVisibility() != null) existingUser.setPostVisibility(user.getPostVisibility());
        if (user.getFriendRequestScope() != null) existingUser.setFriendRequestScope(user.getFriendRequestScope());
        if (user.getMessageScope() != null) existingUser.setMessageScope(user.getMessageScope());
        if (user.getShowActiveStatus() != null) existingUser.setShowActiveStatus(user.getShowActiveStatus());
        if (user.getShowFollowingList() != null) existingUser.setShowFollowingList(user.getShowFollowingList());
        if (user.getShowFriendsList() != null) existingUser.setShowFriendsList(user.getShowFriendsList());
        if (user.getFriendListPrivacy() != null) existingUser.setFriendListPrivacy(user.getFriendListPrivacy());
        if (user.getFollowerListPrivacy() != null) existingUser.setFollowerListPrivacy(user.getFollowerListPrivacy());

        // Chỉ cập nhật URL ảnh khi có giá trị thực, không ghi đè bằng chuỗi rỗng
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank())
            existingUser.setAvatarUrl(user.getAvatarUrl());
        if (user.getBannerUrl() != null && !user.getBannerUrl().isBlank())
            existingUser.setBannerUrl(user.getBannerUrl());

        existingUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existingUser);
    }

    @Override
    public User changePassword(Long id, String oldPassword, String newPassword) {
        if (oldPassword == null || newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("Mật khẩu mới phải có ít nhất 8 ký tự!");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String storedPassword = user.getPassword();
        boolean matches = storedPassword != null && passwordEncoder.matches(oldPassword, storedPassword);

        if (!matches) {
            throw new RuntimeException("Mật khẩu cũ không đúng!");
        }

        // Hash mật khẩu mới rồi lưu
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User login(String username, String rawPassword) {
        String normalizedUsername = username == null ? "" : username.trim();
        User user = userRepository.findByUsername(normalizedUsername)
                .or(() -> userRepository.findByEmail(normalizedUsername.toLowerCase()))
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng!"));

        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
            userRepository.save(user);
        }

        String storedPassword = user.getPassword();
        boolean matches = storedPassword != null && passwordEncoder.matches(rawPassword, storedPassword);

        if (!matches) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng!");
        }

        return user;
    }

    @Override
    public void deleteUser(Long id) {
        if(!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public Map<String, Object> getUserStats(Long userId) {
        List<Post> userPosts = postRepository.findByUserId(userId);
        long totalPosts = userPosts.size();
        long totalViews = 0;
        long totalLikes = 0;

        for (Post p : userPosts) {
            totalViews += p.getViewCount();
            totalLikes += postLikeRepository.countByPostId(p.getId());
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPosts", totalPosts);
        stats.put("totalViews", totalViews);
        stats.put("totalLikes", totalLikes);
        return stats;
    }
}

