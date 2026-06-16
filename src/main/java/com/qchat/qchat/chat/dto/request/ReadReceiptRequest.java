package com.qchat.qchat.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReadReceiptRequest {

    @NotNull
    private UUID conversationId;

    /** Mark all messages up to and including this message as read. */
    @NotNull
    private UUID lastReadMessageId;
}
