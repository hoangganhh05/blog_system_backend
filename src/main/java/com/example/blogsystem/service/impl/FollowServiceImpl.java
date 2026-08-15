package com.example.blogsystem.service.impl;

import com.example.blogsystem.entity.Follow;
import com.example.blogsystem.entity.User;
import com.example.blogsystem.repository.FollowRepository;
import com.example.blogsystem.repository.UserRepository;
import com.example.blogsystem.service.FollowService;
import com.example.blogsystem.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public FollowServiceImpl(FollowRepository followRepository, UserRepository userRepository, NotificationService notificationService) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Map<String, Object> follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("Bạn không thể tự theo dõi chính mình!");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản người theo dõi"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản được theo dõi"));

        Optional<Follow> existing = followRepository.findByFollowerIdAndFollowingId(followerId, followingId);
        if (existing.isEmpty()) {
            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowing(following);
            follow.setCreatedAt(LocalDateTime.now());
            followRepository.save(follow);

            // Gửi thông báo cho người được theo dõi
            String followerName = follower.getFullName() != null ? follower.getFullName() : follower.getUsername();
            notificationService.createNotification(
                    following,
                    follower,
                    null,
                    followerName + " đã bắt đầu theo dõi bạn!"
            );
        }

        Map<String, Object> res = new HashMap<>();
        res.put("isFollowing", true);
        res.put("message", "Đã theo dõi thành công!");
        return res;
    }

    @Override
    public Map<String, Object> unfollow(Long followerId, Long followingId) {
        Optional<Follow> existing = followRepository.findByFollowerIdAndFollowingId(followerId, followingId);
        existing.ifPresent(followRepository::delete);

        Map<String, Object> res = new HashMap<>();
        res.put("isFollowing", false);
        res.put("message", "Đã hủy theo dõi!");
        return res;
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) return false;
        return followRepository.findByFollowerIdAndFollowingId(followerId, followingId).isPresent();
    }

    @Override
    public List<User> getFollowing(Long userId) {
        return followRepository.findFollowingByUserId(userId);
    }

    @Override
    public List<User> getFollowers(Long userId) {
        return followRepository.findFollowersByUserId(userId);
    }

    @Override
    public List<Long> getFollowingIds(Long userId) {
        return followRepository.findFollowingIdsByUserId(userId);
    }

    @Override
    public Map<String, Object> getFollowCounts(Long userId) {
        long followingCount = followRepository.countFollowing(userId);
        long followerCount = followRepository.countFollowers(userId);

        Map<String, Object> res = new HashMap<>();
        res.put("followingCount", followingCount);
        res.put("followerCount", followerCount);
        return res;
    }

    @Override
    public void removeMutualFollow(Long user1, Long user2) {
        followRepository.deleteFollowBetween(user1, user2);
    }
}
