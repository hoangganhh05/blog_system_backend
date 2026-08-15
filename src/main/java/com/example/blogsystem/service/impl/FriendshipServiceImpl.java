package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Friendship;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.FriendshipRepository;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.FriendshipService;
import com.example.blogsystem.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final com.example.blogsystem.service.FollowService followService;

    public FriendshipServiceImpl(FriendshipRepository friendshipRepository, UserRepository userRepository, NotificationService notificationService, com.example.blogsystem.service.FollowService followService) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.followService = followService;
    }

    @Override
    public Map<String, Object> sendFriendRequest(Long requesterId, Long receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new RuntimeException("Không thể gửi lời mời kết bạn cho chính mình!");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(requesterId, receiverId);

        Friendship friendship;
        if (existing.isPresent()) {
            friendship = existing.get();
            if ("ACCEPTED".equals(friendship.getStatus())) {
                throw new RuntimeException("Hai bạn đã là bạn bè!");
            }
            // Đã có lời mời -> cập nhật lại
            friendship.setRequester(requester);
            friendship.setReceiver(receiver);
            friendship.setStatus("PENDING");
            friendship.setCreatedAt(LocalDateTime.now());
        } else {
            friendship = new Friendship();
            friendship.setRequester(requester);
            friendship.setReceiver(receiver);
            friendship.setStatus("PENDING");
            friendship.setCreatedAt(LocalDateTime.now());
        }

        friendshipRepository.save(friendship);

        // Gửi thông báo cho người nhận
        String senderName = requester.getFullName() != null ? requester.getFullName() : requester.getUsername();
        notificationService.createNotification(
                receiver,
                requester,
                null,
                senderName + " đã gửi cho bạn lời mời kết bạn!"
        );

        Map<String, Object> res = new HashMap<>();
        res.put("status", "PENDING_SENT");
        res.put("message", "Đã gửi lời mời kết bạn!");
        return res;
    }

    @Override
    public Map<String, Object> acceptFriendRequest(Long currentUserId, Long requesterId) {
        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(currentUserId, requesterId);
        if (existing.isEmpty()) {
            throw new RuntimeException("Không tìm thấy lời mời kết bạn!");
        }

        Friendship friendship = existing.get();
        friendship.setStatus("ACCEPTED");
        friendshipRepository.save(friendship);

        // Thiết lập trạng thái theo dõi lẫn nhau khi đã trở thành bạn bè
        try {
            followService.follow(currentUserId, requesterId);
            followService.follow(requesterId, currentUserId);
        } catch (Exception ignored) {}

        // Gửi thông báo lại cho người gửi lời mời ban đầu
        User requester = friendship.getRequester();
        User receiver = friendship.getReceiver();
        User senderOfNotice = currentUserId.equals(receiver.getId()) ? receiver : requester;
        User targetOfNotice = currentUserId.equals(receiver.getId()) ? requester : receiver;

        String senderName = senderOfNotice.getFullName() != null ? senderOfNotice.getFullName() : senderOfNotice.getUsername();
        notificationService.createNotification(
                targetOfNotice,
                senderOfNotice,
                null,
                senderName + " đã chấp nhận lời mời kết bạn của bạn!"
        );

        Map<String, Object> res = new HashMap<>();
        res.put("status", "FRIENDS");
        res.put("message", "Đã trở thành bạn bè!");
        return res;
    }

    @Override
    public Map<String, Object> removeOrCancelFriendship(Long userId1, Long userId2) {
        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(userId1, userId2);
        existing.ifPresent(friendshipRepository::delete);

        // Hủy kết bạn (Unfriend) -> tự động hủy luôn trạng thái theo dõi giữa hai tài khoản
        try {
            followService.removeMutualFollow(userId1, userId2);
        } catch (Exception ignored) {}

        Map<String, Object> res = new HashMap<>();
        res.put("status", "NONE");
        res.put("message", "Đã xóa quan hệ bạn bè / lời mời!");
        return res;
    }

    @Override
    public String getFriendshipStatus(Long currentUserId, Long targetUserId) {
        if (currentUserId == null || targetUserId == null) return "NONE";
        if (currentUserId.equals(targetUserId)) return "SELF";

        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(currentUserId, targetUserId);
        if (existing.isEmpty()) return "NONE";

        Friendship f = existing.get();
        if ("ACCEPTED".equals(f.getStatus())) return "FRIENDS";

        if ("PENDING".equals(f.getStatus())) {
            if (f.getRequester().getId().equals(currentUserId)) {
                return "PENDING_SENT"; // Tôi đã gửi lời mời
            } else {
                return "PENDING_RECEIVED"; // Tôi được nhận lời mời từ họ
            }
        }

        return "NONE";
    }

    @Override
    public List<User> getFriendsList(Long userId) {
        List<Friendship> list = friendshipRepository.findAllAcceptedFriends(userId);
        return list.stream()
                .map(f -> f.getRequester().getId().equals(userId) ? f.getReceiver() : f.getRequester())
                .collect(Collectors.toList());
    }

    @Override
    public List<Friendship> getPendingRequests(Long userId) {
        return friendshipRepository.findByReceiverIdAndStatus(userId, "PENDING");
    }

    @Override
    public long getFriendCount(Long userId) {
        return friendshipRepository.countAcceptedFriends(userId);
    }
}
