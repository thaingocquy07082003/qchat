package com.qchat.qchat.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qchat.qchat.chat.enums.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {

    private UUID id;
    private UUID conversationId;

    private UUID senderId;
    private String senderUsername;
    private String senderDisplayName;
    private String senderAvatarUrl;

    private String content;
    private MessageType messageType;

    private String mediaUrl;
    private String mediaThumbnail;
    private Long mediaSizeBytes;

    private ReplyPreview replyTo;

    private boolean edited;
    private boolean deleted;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReplyPreview {
        private UUID id;
        private UUID senderId;
        private String senderUsername;
        private String content;
        private MessageType messageType;
    }
}
