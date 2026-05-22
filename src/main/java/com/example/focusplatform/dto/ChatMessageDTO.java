package com.example.focusplatform.dto;

import com.example.focusplatform.entities.ChatMessage;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private String content;
    private ChatMessage.RoomType targetType;
    private Long targetId;
    private LocalDateTime timestamp;
    // If this is null, the message goes to the General class chat.
    // If it has an ID, it is a Direct Message to that specific user.
    @Column(name = "recipient_id")
    private Long recipientId;

    // Helper method to convert the Database Entity into this safe DTO
    public static ChatMessageDTO fromEntity(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getName()) // We only send the name and ID, NOT the whole user profile!
                .content(message.getContent())
                .targetType(message.getTargetType())
                .targetId(message.getTargetId())
                .timestamp(message.getTimestamp())
                .recipientId(message.getRecipientId())
                .build();
    }
}