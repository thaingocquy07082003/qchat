package com.qchat.qchat.chat.service;

import com.qchat.qchat.chat.dto.request.CallActionRequest;
import com.qchat.qchat.chat.dto.request.InitiateCallRequest;
import com.qchat.qchat.chat.dto.request.WebRtcSignalRequest;
import com.qchat.qchat.chat.dto.response.CallLogResponse;

import java.util.List;
import java.util.UUID;

public interface CallService {

    void initiateCall(UUID callerId, InitiateCallRequest request);

    void acceptCall(UUID userId, UUID callLogId);

    void rejectCall(UUID userId, UUID callLogId);

    void endCall(UUID userId, UUID callLogId);

    void relaySignal(UUID fromUserId, WebRtcSignalRequest request);

    List<CallLogResponse> getCallHistory(UUID conversationId, UUID userId);
}
