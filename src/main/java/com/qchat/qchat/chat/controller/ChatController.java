package com.qchat.qchat.chat.controller;

import com.qchat.qchat.chat.dto.request.ReadReceiptRequest;
import com.qchat.qchat.chat.dto.request.SendMessageRequest;
import com.qchat.qchat.chat.dto.request.TypingRequest;
import com.qchat.qchat.chat.dto.response.MessageResponse;
import com.qchat.qchat.chat.service.MessageService;
import com.qchat.qchat.exception.AppException;
import com.qchat.qchat.exception.ErrorCode;
import com.qchat.qchat.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * WebSocket/STOMP controller.
 *
 * Client connection flow:
 *   1. Connect to /ws (SockJS) with STOMP
 *   2. Include "Authorization: Bearer {jwt}" in CONNECT headers
 *   3. Subscribe to /topic/conversation.{id}           — new messages
 *              and /topic/conversation.{id}.typing      — typing indicators
 *              and /topic/conversation.{id}.read        — read receipts
 *   4. Send to  /app/chat.message                       — send a message
 *              /app/chat.typing                         — notify typing
 *              /app/chat.read                           — mark as read
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final MessageService messageService;

    @MessageMapping("/chat.message")
    public void handleMessage(@Payload SendMessageRequest request, Principal principal) {
        UUID userId = extractUserId(principal);
        MessageResponse response = messageService.sendMessage(userId, request);
        // Broadcasting is handled by RedisMessageSubscriber via Redis Pub/Sub
        log.debug("Message sent by {} to conversation {}", userId, request.getConversationId());
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingRequest request, Principal principal) {
        UUID userId = extractUserId(principal);
        messageService.sendTypingEvent(userId, request);
    }

    @MessageMapping("/chat.read")
    public void handleReadReceipt(@Payload ReadReceiptRequest request, Principal principal) {
        UUID userId = extractUserId(principal);
        messageService.markAsRead(userId, request);
    }

    private UUID extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getId();
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}
