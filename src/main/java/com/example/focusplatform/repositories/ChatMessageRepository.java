package com.example.focusplatform.repositories;

import com.example.focusplatform.entities.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 1. Fetch Class General Chat (Where recipient is null)
    Page<ChatMessage> findByTargetTypeAndTargetIdAndRecipientIdIsNullOrderByTimestampDesc(
            ChatMessage.RoomType targetType,
            Long targetId,
            Pageable pageable
    );

    // 2. Fetch 1-on-1 Direct Messages between two specific users
    @Query("SELECT m FROM ChatMessage m WHERE m.targetType = :type AND m.targetId = :targetId AND " +
            "((m.sender.id = :user1 AND m.recipientId = :user2) OR (m.sender.id = :user2 AND m.recipientId = :user1)) " +
            "ORDER BY m.timestamp DESC")
    Page<ChatMessage> findDirectMessages(
            @Param("type") ChatMessage.RoomType type,
            @Param("targetId") Long targetId,
            @Param("user1") Long user1,
            @Param("user2") Long user2,
            Pageable pageable
    );

    // 3. FIXED: Count unread General Chat messages (EXCLUDING your own messages)
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.targetType = :type AND m.targetId = :targetId AND m.recipientId IS NULL AND m.sender.id != :userId AND m.timestamp > :lastRead")
    long countUnreadGeneralMessages(
            @Param("type") ChatMessage.RoomType type,
            @Param("targetId") Long targetId,
            @Param("userId") Long userId,
            @Param("lastRead") java.time.LocalDateTime lastRead
    );

    // 4. FIXED: Count unread Direct Messages (ONLY counting what the other person sent you)
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.targetType = :type AND m.targetId = :targetId AND m.sender.id = :user2 AND m.recipientId = :user1 AND m.timestamp > :lastRead")
    long countUnreadDirectMessages(
            @Param("type") ChatMessage.RoomType type,
            @Param("targetId") Long targetId,
            @Param("user1") Long user1, // You
            @Param("user2") Long user2, // The other person
            @Param("lastRead") java.time.LocalDateTime lastRead
    );

    // 5. Find all distinct users who have sent a direct message to this user
    @Query("SELECT DISTINCT m.sender.id FROM ChatMessage m WHERE m.targetType = :type AND m.targetId = :targetId AND m.recipientId = :userId")
    List<Long> findDistinctSendersToUser(
            @Param("type") ChatMessage.RoomType type,
            @Param("targetId") Long targetId,
            @Param("userId") Long userId
    );
}