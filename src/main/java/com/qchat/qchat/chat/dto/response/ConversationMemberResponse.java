package com.qchat.qchat.chat.dto.response;

import com.qchat.qchat.chat.enums.MemberRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ConversationMemberResponse {
    private UUID userId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private MemberRole role;
    private boolean online;
}
