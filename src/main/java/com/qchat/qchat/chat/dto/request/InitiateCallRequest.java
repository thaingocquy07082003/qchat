package com.qchat.qchat.chat.dto.request;

import com.qchat.qchat.chat.enums.CallType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class InitiateCallRequest {

    @NotNull
    private UUID conversationId;

    @NotNull
    private CallType callType;
}
