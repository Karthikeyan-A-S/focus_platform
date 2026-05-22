package com.example.focusplatform.services;

import com.example.focusplatform.dto.ChatMessageDTO;
import com.example.focusplatform.entities.ChatMessage;
import com.example.focusplatform.entities.ChatReceipt;
import com.example.focusplatform.entities.User;
import com.example.focusplatform.repositories.ChatMessageRepository;
import com.example.focusplatform.repositories.ChatReceiptRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    // 1. MOVED TO TOP: We declare the new repository here
    private final ChatReceiptRepository chatReceiptRepository;

    // We only keep the last 50 messages in RAM to prevent the server from crashing
    private static final int REDIS_CACHE_SIZE = 50;

    // 2. INJECTED VIA CONSTRUCTOR: We tell Spring Boot to wire all three dependencies
    public ChatService(ChatMessageRepository chatMessageRepository,
                       RedisTemplate<String, Object> redisTemplate,
                       ChatReceiptRepository chatReceiptRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.redisTemplate = redisTemplate;
        this.chatReceiptRepository = chatReceiptRepository;
    }

    @Transactional
    public ChatMessageDTO processAndSaveMessage(User sender, ChatMessageDTO messageDTO) {
        // 1. Convert DTO to Entity (Now includes recipientId)
        ChatMessage entity = ChatMessage.builder()
                .sender(sender)
                .content(messageDTO.getContent())
                .targetType(messageDTO.getTargetType())
                .targetId(messageDTO.getTargetId())
                .recipientId(messageDTO.getRecipientId())
                .build();

        ChatMessage savedEntity = chatMessageRepository.save(entity);
        ChatMessageDTO savedDTO = ChatMessageDTO.fromEntity(savedEntity);

        // 2. Push to Redis Cache using the dynamic private/public key
        String redisKey = buildRedisKey(savedDTO.getTargetType(), savedDTO.getTargetId(), savedDTO.getRecipientId(), sender.getId());
        redisTemplate.opsForList().rightPush(redisKey, savedDTO);
        redisTemplate.opsForList().trim(redisKey, -REDIS_CACHE_SIZE, -1);

        return savedDTO;
    }

    public List<ChatMessageDTO> getChatHistory(ChatMessage.RoomType targetType, Long targetId, Long recipientId, Long currentUserId, Pageable pageable) {
        String redisKey = buildRedisKey(targetType, targetId, recipientId, currentUserId);

        // OPTIMIZATION: Try Redis first!
        if (pageable.getPageNumber() == 0) {
            long size = redisTemplate.opsForList().size(redisKey) != null ? redisTemplate.opsForList().size(redisKey) : 0;
            if (size > 0) {
                List<Object> cachedMessages = redisTemplate.opsForList().range(redisKey, 0, -1);
                if (cachedMessages != null && !cachedMessages.isEmpty()) {
                    return cachedMessages.stream().map(obj -> (ChatMessageDTO) obj).collect(Collectors.toList());
                }
            }
        }

        // FALLBACK: Hit the SQL Database using our new Repository methods
        if (recipientId == null) {
            // Fetch General Class Chat
            return chatMessageRepository.findByTargetTypeAndTargetIdAndRecipientIdIsNullOrderByTimestampDesc(
                            targetType, targetId, pageable)
                    .stream().map(ChatMessageDTO::fromEntity).collect(Collectors.toList());
        } else {
            // Fetch 1-on-1 Direct Message
            return chatMessageRepository.findDirectMessages(
                            targetType, targetId, currentUserId, recipientId, pageable)
                    .stream().map(ChatMessageDTO::fromEntity).collect(Collectors.toList());
        }
    }

    private String buildRedisKey(ChatMessage.RoomType type, Long targetId, Long recipientId, Long senderId) {
        if (type == ChatMessage.RoomType.GENERAL) {
            return "chat:history:GENERAL";
        }
        if (recipientId == null) {
            return "chat:history:CLASSROOM:" + targetId;
        }
        // For DMs, use min/max to ensure both users look at the exact same cache key
        Long minId = Math.min(senderId, recipientId);
        Long maxId = Math.max(senderId, recipientId);
        return "chat:history:CLASSROOM:" + targetId + ":DM:" + minId + "_" + maxId;
    }

    // --- WATERMARKING METHODS ---

    // Inside markAsRead method (Around line 107)
    @Transactional
    public void markAsRead(Long userId, ChatMessage.RoomType targetType, Long targetId, Long dmPartnerId) {
        ChatReceipt receipt = chatReceiptRepository.findFirstByUserIdAndTargetTypeAndTargetIdAndDmPartnerId(
                userId, targetType, targetId, dmPartnerId
        ).orElse(ChatReceipt.builder()
                .userId(userId)
                .targetType(targetType)
                .targetId(targetId)
                .dmPartnerId(dmPartnerId)
                .build());

        receipt.setLastReadAt(java.time.LocalDateTime.now());
        chatReceiptRepository.save(receipt);
    }

    public long getUnreadCount(Long userId, ChatMessage.RoomType type, Long targetId, Long dmPartnerId) {
        java.time.LocalDateTime lastRead = chatReceiptRepository
                .findFirstByUserIdAndTargetTypeAndTargetIdAndDmPartnerId(userId, type, targetId, dmPartnerId)
                .map(ChatReceipt::getLastReadAt)
                .orElse(java.time.LocalDateTime.of(2000, 1, 1, 0, 0));

        if (dmPartnerId == null) {
            return chatMessageRepository.countUnreadGeneralMessages(type, targetId, userId, lastRead);
        } else {
            return chatMessageRepository.countUnreadDirectMessages(type, targetId, userId, dmPartnerId, lastRead);
        }
    }
    public long getTotalUnreadCountForClassroom(Long userId, ChatMessage.RoomType type, Long targetId) {
        // 1. Get the General unread count
        long total = getUnreadCount(userId, type, targetId, null);

        // 2. Find everyone who has PM'd this user, and add their unread counts
        List<Long> dmSenders = chatMessageRepository.findDistinctSendersToUser(type, targetId, userId);
        for (Long senderId : dmSenders) {
            total += getUnreadCount(userId, type, targetId, senderId);
        }

        return total;
    }
}