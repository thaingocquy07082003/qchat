package com.qchat.qchat.chat.controller;

import com.qchat.qchat.chat.dto.request.CallActionRequest;
import com.qchat.qchat.chat.dto.request.InitiateCallRequest;
import com.qchat.qchat.chat.dto.request.WebRtcSignalRequest;
import com.qchat.qchat.chat.service.CallService;
import com.qchat.qchat.exception.AppException;
import com.qchat.qchat.exception.ErrorCode;
import com.qchat.qchat.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * WebSocket/STOMP handlers for WebRTC call signaling.
 *
 * Client sends to:
 *   /app/call.initiate   – start a voice or video call in a conversation
 *   /app/call.accept     – accept an incoming call
 *   /app/call.reject     – decline an incoming call
 *   /app/call.end        – leave / end a call
 *   /app/call.signal     – relay a WebRTC offer / answer / ICE candidate to a specific peer
 *
 * Client subscribes to:
 *   /topic/conversation.{id}.call   – broadcast call events (INCOMING_CALL, CALL_ACCEPTED, …)
 *   /user/queue/call.signal         – directed WebRTC signals (OFFER, ANSWER, ICE_CANDIDATE)
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class CallController {

    private final CallService callService;

    @MessageMapping("/call.initiate")
    public void initiateCall(@Payload InitiateCallRequest request, Principal principal) {
        UUID userId = extractUserId(principal);
        log.info("[WS] call.initiate | user={} | conv={} | type={}", userId, request.getConversationId(), request.getCallType());
        callService.initiateCall(userId, request);
    }

    @MessageMapping("/call.accept")
    public void acceptCall(@Payload CallActionRequest request, Principal principal) {
        UUID userId = extractUserId(principal);
        log.info("[WS] call.accept | user={} | callId={}", userId, request.getCallLogId());
        callService.acceptCall(userId, request.getCallLogId());
    }

    @MessageMapping("/call.reject")
    public void rejectCall(@Payload CallActionRequest request, Principal principal) {
        UUID userId = extractUserId(principal);
        log.info("[WS] call.reject | user={} | callId={}", userId, request.getCallLogId());
        callService.rejectCall(userId, request.getCallLogId());
    }

    @MessageMapping("/call.end")
    public void endCall(@Payload CallActionRequest request, Principal principal) {
        UUID userId = extractUserId(principal);
        log.info("[WS] call.end | user={} | callId={}", userId, request.getCallLogId());
        callService.endCall(userId, request.getCallLogId());
    }

    @MessageMapping("/call.signal")
    public void relaySignal(@Payload WebRtcSignalRequest request, Principal principal) {
        UUID userId = extractUserId(principal);
        log.debug("[WS] call.signal | user={} | to={} | type={}", userId, request.getToUserId(), request.getSignalType());
        callService.relaySignal(userId, request);
    }

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidationError(MethodArgumentNotValidException ex) {
        log.warn("[WS] call signal validation failed: {}", ex.getMessage());
    }

    @MessageExceptionHandler(Exception.class)
    public void handleError(Exception ex) {
        log.warn("[WS] call handler error: {}", ex.getMessage());
    }

    private UUID extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getId();
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}
