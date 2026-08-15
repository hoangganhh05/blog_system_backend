package com.example.blogsystem.repository;

import com.example.blogsystem.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Lấy lịch sử trò chuyện giữa 2 người dùng (sắp xếp tăng dần theo thời gian)
    @Query("SELECT m FROM ChatMessage m WHERE (m.sender.id = :u1 AND m.receiver.id = :u2) OR (m.sender.id = :u2 AND m.receiver.id = :u1) ORDER BY m.createdAt ASC")
    List<ChatMessage> findChatHistory(@Param("u1") Long u1, @Param("u2") Long u2);

    // Kiểm tra xem 2 người dùng đã từng có lịch sử nhắn tin cho nhau chưa
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM ChatMessage m WHERE (m.sender.id = :u1 AND m.receiver.id = :u2) OR (m.sender.id = :u2 AND m.receiver.id = :u1)")
    boolean existsBetweenUsers(@Param("u1") Long u1, @Param("u2") Long u2);

    // Đếm số tin nhắn chưa đọc từ 1 người gửi đến 1 người nhận
    long countBySenderIdAndReceiverIdAndIsReadFalse(Long senderId, Long receiverId);

    // Lấy tất cả tin nhắn chưa đọc gửi cho 1 người nhận
    List<ChatMessage> findBySenderIdAndReceiverIdAndIsReadFalse(Long senderId, Long receiverId);
}
