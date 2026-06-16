package com.qchat.qchat.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.qchat.qchat.chat.enums.ConversationType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationResponse {

    private UUID id;
    private ConversationType type;

    /** Group: group name. Direct: the other user's display name. */
    private String name;

    /** Group: group avatar. Direct: the other user's avatar. */
    private String avatarUrl;

    private String description;

    private OffsetDateTime lastMessageAt;
    private MessageResponse lastMessage;
    private long unreadCount;

    private List<ConversationMemberResponse> members;
}
