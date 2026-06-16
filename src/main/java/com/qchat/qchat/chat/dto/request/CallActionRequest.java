package com.qchat.qchat.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CallActionRequest {

    @NotNull
    private UUID callLogId;
}
