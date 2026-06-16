package com.qchat.qchat.chat.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMessageSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ChatEvent event = objectMapper.readValue(message.getBody(), ChatEvent.class);
            String destination = resolveDestination(event);
            messagingTemplate.convertAndSend(destination, event.getData());
        } catch (Exception e) {
            log.error("Failed to process Redis chat event: {}", e.getMessage());
        }
    }

    private String resolveDestination(ChatEvent event) {
        return switch (event.getType()) {
            case "TYPING"       -> "/topic/conversation." + event.getConversationId() + ".typing";
            case "READ_RECEIPT" -> "/topic/conversation." + event.getConversationId() + ".read";
            case "CALL_EVENT"   -> "/topic/conversation." + event.getConversationId() + ".call";
            default             -> "/topic/conversation." + event.getConversationId();
        };
    }
}
