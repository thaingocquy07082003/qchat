package com.qchat.qchat.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Carries a WebRTC signaling message (offer / answer / ICE candidate)
 * from one peer to another.
 *
 * signalType: "OFFER" | "ANSWER" | "ICE_CANDIDATE"
 * payload:    JSON string of RTCSessionDescription or RTCIceCandidate
 */
@Data
public class WebRtcSignalRequest {

    @NotNull
    private UUID callLogId;

    @NotNull
    private UUID toUserId;

    @NotBlank
    private String signalType;

    @NotBlank
    private String payload;
}
