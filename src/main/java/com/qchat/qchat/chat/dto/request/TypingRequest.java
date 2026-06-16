package com.qchat.qchat.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class TypingRequest {

    @NotNull
    private UUID conversationId;

    private boolean typing;
}
