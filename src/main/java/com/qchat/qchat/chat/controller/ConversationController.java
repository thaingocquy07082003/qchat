package com.qchat.qchat.chat.controller;

import com.qchat.qchat.chat.dto.request.CreateConversationRequest;
import com.qchat.qchat.chat.dto.request.CreateDirectConversationRequest;
import com.qchat.qchat.chat.dto.response.ConversationResponse;
import com.qchat.qchat.chat.dto.response.MessageResponse;
import com.qchat.qchat.chat.service.ConversationService;
import com.qchat.qchat.chat.service.MessageService;
import com.qchat.qchat.common.ApiResponse;
import com.qchat.qchat.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService      messageService;

    /** Start a 1-1 direct conversation. Returns existing one if already created. */
    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<ConversationResponse>> createDirect(
            @Valid @RequestBody CreateDirectConversationRequest request,
            @AuthenticationPrincipal CustomUserDetails me) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.createDirectConversation(me.getId(), request)));
    }

    /** Create a new group conversation. */
    @PostMapping("/group")
    public ResponseEntity<ApiResponse<ConversationResponse>> createGroup(
            @Valid @RequestBody CreateConversationRequest request,
            @AuthenticationPrincipal CustomUserDetails me) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.createGroupConversation(me.getId(), request)));
    }

    /** List all conversations the current user belongs to. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversations(
            @AuthenticationPrincipal CustomUserDetails me) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.getUserConversations(me.getId())));
    }

    /** Get a single conversation. */
    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal CustomUserDetails me) {
        return ResponseEntity.ok(ApiResponse.success(
                conversationService.getConversation(conversationId, me.getId())));
    }

    /** Paginated message history — latest first. */
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal CustomUserDetails me) {
        return ResponseEntity.ok(ApiResponse.success(
                messageService.getMessages(conversationId, me.getId(), page, size)));
    }

    /** Add members to a group (admin only). */
    @PostMapping("/{conversationId}/members")
    public ResponseEntity<ApiResponse<Void>> addMembers(
            @PathVariable UUID conversationId,
            @RequestBody List<UUID> memberIds,
            @AuthenticationPrincipal CustomUserDetails me) {
        conversationService.addMembersToGroup(conversationId, memberIds, me.getId());
        return ResponseEntity.ok(ApiResponse.ok("Members added"));
    }

    /** Remove a member from a group (admin only). */
    @DeleteMapping("/{conversationId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID conversationId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails me) {
        conversationService.removeMemberFromGroup(conversationId, userId, me.getId());
        return ResponseEntity.ok(ApiResponse.ok("Member removed"));
    }

    /** Leave a group conversation. */
    @DeleteMapping("/{conversationId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveGroup(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal CustomUserDetails me) {
        conversationService.leaveGroup(conversationId, me.getId());
        return ResponseEntity.ok(ApiResponse.ok("Left conversation"));
    }
}
