package com.example.focusplatform.controllers;

import com.example.focusplatform.dto.ChatMessageDTO;
import com.example.focusplatform.entities.ChatMessage;
import com.example.focusplatform.entities.User;
import com.example.focusplatform.repositories.UserRepository;
import com.example.focusplatform.services.ChatService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserRepository userRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate,
                          ChatService chatService,
                          UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    /**
     * WebSocket Endpoint: Receives real-time incoming messages from clients.
     * Frontend sends to: /app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO messageDTO, Principal principal) {
        if (principal == null) return;
        User sender = userRepository.findByEmail(principal.getName()).orElseThrow();
        ChatMessageDTO savedMessage = chatService.processAndSaveMessage(sender, messageDTO);

        if (savedMessage.getTargetType() == ChatMessage.RoomType.GENERAL) {
            messagingTemplate.convertAndSend("/topic/general", savedMessage);
        } else if (savedMessage.getTargetType() == ChatMessage.RoomType.CLASSROOM) {

            if (savedMessage.getRecipientId() == null) {
                // Route to Class General
                messagingTemplate.convertAndSend("/topic/class/" + savedMessage.getTargetId(), savedMessage);
            } else {
                // Route to 1-on-1 Private DM
                // We create a unique room string using the smallest ID first (e.g., "3_5")
                Long minId = Math.min(sender.getId(), savedMessage.getRecipientId());
                Long maxId = Math.max(sender.getId(), savedMessage.getRecipientId());
                String privateRoom = "/topic/class/" + savedMessage.getTargetId() + "/dm/" + minId + "_" + maxId;

                messagingTemplate.convertAndSend(privateRoom, savedMessage);
            }
        }
    }

    /**
     * HTTP REST Endpoint: Fetches reverse-paginated historical chat messages.
     */
    @GetMapping("/history/{targetType}/{targetId}")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(
            @PathVariable("targetType") ChatMessage.RoomType targetType,
            @PathVariable("targetId") Long targetId,
            @RequestParam(value = "recipientId", required = false) Long recipientId, // <-- NEW
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "30") int size,
            Principal principal) { // <-- NEW: We need to know who is asking for the history!

        if (principal == null) return ResponseEntity.status(401).build();

        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        // Pass the new variables into our updated service method
        List<ChatMessageDTO> history = chatService.getChatHistory(
                targetType, targetId, recipientId, currentUser.getId(), pageable
        );

        return ResponseEntity.ok(history);
    }

    /**
     * Called by React when a user clicks on a chat tab to update their "Last Read" watermark.
     */
    @PostMapping("/read")
    public ResponseEntity<Void> markAsRead(
            @RequestParam("targetType") ChatMessage.RoomType targetType,
            @RequestParam("targetId") Long targetId,
            @RequestParam(value = "dmPartnerId", required = false) Long dmPartnerId,
            Principal principal) {

        if (principal == null) return ResponseEntity.status(401).build();
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();

        chatService.markAsRead(currentUser.getId(), targetType, targetId, dmPartnerId);
        return ResponseEntity.ok().build();
    }

    /**
     * Called by React to get the number to display inside the red notification badge.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @RequestParam("targetType") ChatMessage.RoomType targetType,
            @RequestParam("targetId") Long targetId,
            @RequestParam(value = "dmPartnerId", required = false) Long dmPartnerId,
            Principal principal) {

        if (principal == null) return ResponseEntity.status(401).build();
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();

        long count = chatService.getUnreadCount(currentUser.getId(), targetType, targetId, dmPartnerId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/unread-total")
    public ResponseEntity<Long> getTotalUnreadCount(
            @RequestParam("targetType") ChatMessage.RoomType targetType,
            @RequestParam("targetId") Long targetId,
            Principal principal) {

        if (principal == null) return ResponseEntity.status(401).build();
        User currentUser = userRepository.findByEmail(principal.getName()).orElseThrow();

        long count = chatService.getTotalUnreadCountForClassroom(currentUser.getId(), targetType, targetId);
        return ResponseEntity.ok(count);
    }

}