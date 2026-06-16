package com.qchat.qchat.chat.dto.request;

import com.qchat.qchat.chat.enums.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessageRequest {

    @NotNull
    private UUID conversationId;

    private String content;

    private MessageType messageType = MessageType.TEXT;

    private String mediaUrl;

    private String mediaThumbnail;

    private Long mediaSizeBytes;

    private UUID replyToId;
}
