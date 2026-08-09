package com.example.blogsystem.repository;

import com.example.blogsystem.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Tìm mối quan hệ bạn bè giữa 2 người dùng (bất kể ai gửi)
    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :u1 AND f.receiver.id = :u2) OR (f.requester.id = :u2 AND f.receiver.id = :u1)")
    Optional<Friendship> findFriendshipBetween(@Param("u1") Long u1, @Param("u2") Long u2);

    // Lấy danh sách các lời mời kết bạn ĐANG CHỜ của 1 người
    List<Friendship> findByReceiverIdAndStatus(Long receiverId, String status);

    // Lấy tất cả các quan hệ ĐÃ KẾT BẠN (ACCEPTED) của 1 người
    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :userId OR f.receiver.id = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAllAcceptedFriends(@Param("userId") Long userId);

    // Đếm số lượng bạn bè
    @Query("SELECT COUNT(f) FROM Friendship f WHERE (f.requester.id = :userId OR f.receiver.id = :userId) AND f.status = 'ACCEPTED'")
    long countAcceptedFriends(@Param("userId") Long userId);
}
