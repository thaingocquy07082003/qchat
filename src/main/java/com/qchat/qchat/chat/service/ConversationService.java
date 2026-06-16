package com.qchat.qchat.chat.service;

import com.qchat.qchat.chat.dto.request.CreateConversationRequest;
import com.qchat.qchat.chat.dto.request.CreateDirectConversationRequest;
import com.qchat.qchat.chat.dto.response.ConversationResponse;

import java.util.List;
import java.util.UUID;

public interface ConversationService {

    ConversationResponse createDirectConversation(UUID currentUserId, CreateDirectConversationRequest request);

    ConversationResponse createGroupConversation(UUID currentUserId, CreateConversationRequest request);

    List<ConversationResponse> getUserConversations(UUID userId);

    ConversationResponse getConversation(UUID conversationId, UUID requestingUserId);

    void addMembersToGroup(UUID conversationId, List<UUID> userIds, UUID requestingUserId);

    void removeMemberFromGroup(UUID conversationId, UUID targetUserId, UUID requestingUserId);

    void leaveGroup(UUID conversationId, UUID userId);
}
