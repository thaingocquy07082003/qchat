package com.qchat.qchat.chat.service;

import com.qchat.qchat.chat.dto.request.ReadReceiptRequest;
import com.qchat.qchat.chat.dto.request.SendMessageRequest;
import com.qchat.qchat.chat.dto.request.TypingRequest;
import com.qchat.qchat.chat.dto.response.MessageResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MessageService {

    MessageResponse sendMessage(UUID senderId, SendMessageRequest request);

    Page<MessageResponse> getMessages(UUID conversationId, UUID requestingUserId, int page, int size);

    void markAsRead(UUID userId, ReadReceiptRequest request);

    void sendTypingEvent(UUID userId, TypingRequest request);
}
