package com.example.focusplatform.repositories;

import com.example.focusplatform.entities.ChatReceipt;
import com.example.focusplatform.entities.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChatReceiptRepository extends JpaRepository<ChatReceipt, Long> {

    // ADDED "First" to the method name to prevent crashes if duplicates exist
    Optional<ChatReceipt> findFirstByUserIdAndTargetTypeAndTargetIdAndDmPartnerId(
            Long userId, ChatMessage.RoomType targetType, Long targetId, Long dmPartnerId
    );
}