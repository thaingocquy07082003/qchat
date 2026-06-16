package com.qchat.qchat.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateConversationRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    private String avatarUrl;

    private String description;

    @NotEmpty
    private List<UUID> memberIds;
}
