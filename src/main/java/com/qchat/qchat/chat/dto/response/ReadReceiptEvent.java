package com.qchat.qchat.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadReceiptEvent {
    private UUID conversationId;
    private UUID userId;
    private String username;
    private UUID lastReadMessageId;
    private OffsetDateTime readAt;
}
