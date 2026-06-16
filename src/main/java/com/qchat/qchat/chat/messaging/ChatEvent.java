package com.qchat.qchat.chat.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Envelope published to Redis Pub/Sub.
 * type: NEW_MESSAGE | TYPING | READ_RECEIPT
 * data: already-serialized JSON string of the actual payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEvent {
    private String type;
    private UUID conversationId;
    private Object data;
}
