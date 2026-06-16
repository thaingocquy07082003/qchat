package com.qchat.qchat.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingEvent {
    private UUID conversationId;
    private UUID userId;
    private String username;
    private boolean typing;
}
