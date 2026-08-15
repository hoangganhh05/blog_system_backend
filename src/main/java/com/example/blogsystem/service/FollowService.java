package com.example.blogsystem.service;

import com.example.blogsystem.entity.User;

import java.util.List;
import java.util.Map;

public interface FollowService {

    // Theo dõi người dùng
    Map<String, Object> follow(Long followerId, Long followingId);

    // Hủy theo dõi người dùng
    Map<String, Object> unfollow(Long followerId, Long followingId);

    // Kiểm tra trạng thái theo dõi
    boolean isFollowing(Long followerId, Long followingId);

    // Lấy danh sách những người đang theo dõi
    List<User> getFollowing(Long userId);

    // Lấy danh sách người theo dõi
    List<User> getFollowers(Long userId);

    // Lấy danh sách ID những người đang theo dõi
    List<Long> getFollowingIds(Long userId);

    // Đếm số lượng
    Map<String, Object> getFollowCounts(Long userId);

    // Hủy toàn bộ theo dõi 2 chiều khi Hủy kết bạn
    void removeMutualFollow(Long user1, Long user2);
}
