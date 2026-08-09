package com.example.blogsystem.service;

import com.example.blogsystem.entity.Friendship;
import com.example.blogsystem.entity.User;

import java.util.List;
import java.util.Map;

public interface FriendshipService {
    Map<String, Object> sendFriendRequest(Long requesterId, Long receiverId);
    Map<String, Object> acceptFriendRequest(Long requesterId, Long receiverId);
    Map<String, Object> removeOrCancelFriendship(Long userId1, Long userId2);
    String getFriendshipStatus(Long currentUserId, Long targetUserId);
    List<User> getFriendsList(Long userId);
    List<Friendship> getPendingRequests(Long userId);
    long getFriendCount(Long userId);
}
