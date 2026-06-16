package com.qchat.qchat.chat.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMessagePublisher {

    private static final String CHANNEL_PREFIX = "chat:conv:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(UUID conversationId, String type, Object payload) {
        try {
            ChatEvent event = ChatEvent.builder()
                    .type(type)
                    .conversationId(conversationId)
                    .data(payload)
                    .build();
            String json = objectMapper.writeValueAsString(event);
            stringRedisTemplate.convertAndSend(CHANNEL_PREFIX + conversationId, json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize chat event for conversation {}: {}", conversationId, e.getMessage());
        }
    }
}
