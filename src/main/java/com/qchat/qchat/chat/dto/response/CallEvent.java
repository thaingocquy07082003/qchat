package com.qchat.qchat.chat.dto.response;

import com.qchat.qchat.chat.enums.CallType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Pushed over WebSocket to all conversation subscribers
 * on /topic/conversation.{id}.call  (broadcast events)
 * or /user/queue/call.signal        (directed WebRTC signals)
 *
 * type values:
 *   INCOMING_CALL   – new call started
 *   CALL_ACCEPTED   – a participant joined
 *   CALL_REJECTED   – a member declined
 *   PARTICIPANT_LEFT – one peer left but call continues
 *   CALL_ENDED      – last participant left, call over
 *   OFFER           – WebRTC SDP offer  (directed)
 *   ANSWER          – WebRTC SDP answer (directed)
 *   ICE_CANDIDATE   – WebRTC ICE candidate (directed)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallEvent {

    private String type;
    private UUID callLogId;
    private UUID conversationId;
    private UUID fromUserId;
    private String senderUsername;
    private String senderDisplayName;
    private String senderAvatarUrl;
    private CallType callType;
    private int durationSeconds;
    private String payload;
    private OffsetDateTime timestamp;
}
