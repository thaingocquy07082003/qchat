package com.qchat.qchat.chat.dto.response;

import com.qchat.qchat.chat.enums.CallStatus;
import com.qchat.qchat.chat.enums.CallType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallLogResponse {

    private UUID id;
    private UUID conversationId;
    private UUID callerId;
    private String callerUsername;
    private String callerDisplayName;
    private String callerAvatarUrl;
    private CallType callType;
    private CallStatus status;
    private OffsetDateTime startedAt;
    private OffsetDateTime answeredAt;
    private OffsetDateTime endedAt;
    private int durationSeconds;
    private List<ParticipantInfo> participants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        private UUID userId;
        private String username;
        private String displayName;
        private OffsetDateTime joinedAt;
        private OffsetDateTime leftAt;
    }
}
