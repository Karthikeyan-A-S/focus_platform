package com.example.focusplatform.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_receipts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatReceipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // The person who owns this receipt

    @Enumerated(EnumType.STRING)
    private ChatMessage.RoomType targetType; // e.g., CLASSROOM

    private Long targetId; // Classroom ID

    private Long dmPartnerId; // Null if General chat, User ID if Direct Message

    private LocalDateTime lastReadAt; // The Watermark!
}