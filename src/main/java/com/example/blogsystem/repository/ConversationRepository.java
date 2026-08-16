package com.example.blogsystem.repository;

import com.example.blogsystem.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c LEFT JOIN FETCH c.user1 LEFT JOIN FETCH c.user2 " +
           "WHERE (c.user1.id = :u1 AND c.user2.id = :u2) " +
           "   OR (c.user1.id = :u2 AND c.user2.id = :u1)")
    Optional<Conversation> findBetweenUsers(@Param("u1") Long u1, @Param("u2") Long u2);
}
