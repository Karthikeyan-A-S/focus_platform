package com.example.focusplatform.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who sent the message
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // The actual text message
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Tells us if this belongs to the global chat or a specific class
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType targetType;

    // If RoomType is CLASSROOM, this holds the Classroom ID.
    // If RoomType is GENERAL, this will be null.
    @Column(name = "target_id")
    private Long targetId;

    // Automatically stamps the exact time the message hits the database
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public enum RoomType {
        GENERAL,
        CLASSROOM
    }
    // If this is null, the message goes to the General class chat.
    // If it has an ID, it is a Direct Message to that specific user.
    @Column(name = "recipient_id")
    private Long recipientId;
}