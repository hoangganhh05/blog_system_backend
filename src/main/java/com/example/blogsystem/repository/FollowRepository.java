package com.example.blogsystem.repository;

import com.example.blogsystem.entity.Follow;
import com.example.blogsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // Kiểm tra xem followerId có đang theo dõi followingId không
    @Query("SELECT f FROM Follow f WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    Optional<Follow> findByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    // Lấy danh sách những người mà userId đang theo dõi (User is follower)
    @Query("SELECT f.following FROM Follow f WHERE f.follower.id = :userId")
    List<User> findFollowingByUserId(@Param("userId") Long userId);

    // Lấy danh sách những người đang theo dõi userId (User is following)
    @Query("SELECT f.follower FROM Follow f WHERE f.following.id = :userId")
    List<User> findFollowersByUserId(@Param("userId") Long userId);

    // Lấy danh sách ID những người mà userId đang theo dõi
    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId")
    List<Long> findFollowingIdsByUserId(@Param("userId") Long userId);

    // Đếm số lượng người mà userId đang theo dõi
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId")
    long countFollowing(@Param("userId") Long userId);

    // Đếm số lượng người theo dõi userId
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.id = :userId")
    long countFollowers(@Param("userId") Long userId);

    // Xóa quan hệ theo dõi giữa 2 người
    @Modifying
    @Query("DELETE FROM Follow f WHERE (f.follower.id = :u1 AND f.following.id = :u2) OR (f.follower.id = :u2 AND f.following.id = :u1)")
    void deleteFollowBetween(@Param("u1") Long u1, @Param("u2") Long u2);
}
