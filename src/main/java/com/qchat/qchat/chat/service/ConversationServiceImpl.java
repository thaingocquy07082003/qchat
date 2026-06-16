package com.qchat.qchat.chat.service;

import com.qchat.qchat.auth.entity.User;
import com.qchat.qchat.auth.repository.UserRepository;
import com.qchat.qchat.chat.dto.request.CreateConversationRequest;
import com.qchat.qchat.chat.dto.request.CreateDirectConversationRequest;
import com.qchat.qchat.chat.dto.response.ConversationMemberResponse;
import com.qchat.qchat.chat.dto.response.ConversationResponse;
import com.qchat.qchat.chat.dto.response.MessageResponse;
import com.qchat.qchat.chat.entity.Conversation;
import com.qchat.qchat.chat.entity.ConversationMember;
import com.qchat.qchat.chat.enums.ConversationType;
import com.qchat.qchat.chat.enums.MemberRole;
import com.qchat.qchat.chat.enums.MessageType;
import com.qchat.qchat.chat.repository.ConversationMemberRepository;
import com.qchat.qchat.chat.repository.ConversationRepository;
import com.qchat.qchat.chat.repository.MessageRepository;
import com.qchat.qchat.exception.AppException;
import com.qchat.qchat.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository     conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageRepository          messageRepository;
    private final UserRepository             userRepository;
    private final PresenceService            presenceService;

    @Override
    public ConversationResponse createDirectConversation(UUID currentUserId, CreateDirectConversationRequest request) {
        UUID targetId = request.getTargetUserId();
        if (currentUserId.equals(targetId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST_DATA);
        }

        return conversationRepository.findDirectConversation(currentUserId, targetId)
                .map(existing -> toResponse(existing, currentUserId))
                .orElseGet(() -> {
                    User me     = getUser(currentUserId);
                    User target = getUser(targetId);

                    Conversation conv = conversationRepository.save(
                            Conversation.builder().type(ConversationType.DIRECT).createdBy(me).build());

                    memberRepository.save(member(conv, me,     MemberRole.MEMBER));
                    memberRepository.save(member(conv, target, MemberRole.MEMBER));

                    return toResponse(conv, currentUserId);
                });
    }

    @Override
    public ConversationResponse createGroupConversation(UUID currentUserId, CreateConversationRequest request) {
        User creator = getUser(currentUserId);

        Conversation conv = conversationRepository.save(Conversation.builder()
                .type(ConversationType.GROUP)
                .name(request.getName())
                .avatarUrl(request.getAvatarUrl())
                .description(request.getDescription())
                .createdBy(creator)
                .build());

        memberRepository.save(member(conv, creator, MemberRole.ADMIN));

        for (UUID memberId : request.getMemberIds()) {
            if (!memberId.equals(currentUserId)) {
                User u = getUser(memberId);
                memberRepository.save(member(conv, u, MemberRole.MEMBER));
            }
        }

        return toResponse(conv, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(UUID userId) {
        return conversationRepository.findAllByMemberUserId(userId).stream()
                .map(c -> toResponse(c, userId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversation(UUID conversationId, UUID requestingUserId) {
        assertMember(conversationId, requestingUserId);
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return toResponse(conv, requestingUserId);
    }

    @Override
    public void addMembersToGroup(UUID conversationId, List<UUID> userIds, UUID requestingUserId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (conv.getType() != ConversationType.GROUP) throw new AppException(ErrorCode.INVALID_REQUEST_DATA);
        assertAdmin(conversationId, requestingUserId);

        for (UUID uid : userIds) {
            memberRepository.findByConversationIdAndUserId(conversationId, uid)
                    .ifPresentOrElse(
                            m -> { m.setActive(true); m.setLeftAt(null); },
                            () -> memberRepository.save(member(conv, getUser(uid), MemberRole.MEMBER))
                    );
        }
    }

    @Override
    public void removeMemberFromGroup(UUID conversationId, UUID targetUserId, UUID requestingUserId) {
        assertAdmin(conversationId, requestingUserId);
        ConversationMember m = memberRepository.findByConversationIdAndUserId(conversationId, targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        m.setActive(false);
        m.setLeftAt(OffsetDateTime.now());
    }

    @Override
    public void leaveGroup(UUID conversationId, UUID userId) {
        ConversationMember m = memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        m.setActive(false);
        m.setLeftAt(OffsetDateTime.now());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private ConversationResponse toResponse(Conversation conv, UUID viewerUserId) {
        List<ConversationMember> activeMembers =
                memberRepository.findByConversationIdAndIsActiveTrue(conv.getId());

        List<ConversationMemberResponse> memberResponses = activeMembers.stream()
                .map(m -> ConversationMemberResponse.builder()
                        .userId(m.getUser().getId())
                        .username(m.getUser().getUsername())
                        .displayName(m.getUser().getProfile() != null
                                ? m.getUser().getProfile().getDisplayName() : m.getUser().getUsername())
                        .avatarUrl(m.getUser().getProfile() != null
                                ? m.getUser().getProfile().getAvatarUrl() : null)
                        .role(m.getRole())
                        .online(presenceService.isOnline(m.getUser().getId()))
                        .build())
                .toList();

        String name      = conv.getName();
        String avatarUrl = conv.getAvatarUrl();

        if (conv.getType() == ConversationType.DIRECT) {
            ConversationMember other = activeMembers.stream()
                    .filter(m -> !m.getUser().getId().equals(viewerUserId))
                    .findFirst()
                    .orElse(null);
            if (other != null) {
                name      = other.getUser().getProfile() != null
                        ? other.getUser().getProfile().getDisplayName()
                        : other.getUser().getUsername();
                avatarUrl = other.getUser().getProfile() != null
                        ? other.getUser().getProfile().getAvatarUrl() : null;
            }
        }

        MessageResponse lastMsg = messageRepository.findLastMessage(conv.getId())
                .map(this::toMessageResponse)
                .orElse(null);

        long unread = memberRepository.countUnreadMessages(conv.getId(), viewerUserId);

        return ConversationResponse.builder()
                .id(conv.getId())
                .type(conv.getType())
                .name(name)
                .avatarUrl(avatarUrl)
                .description(conv.getDescription())
                .lastMessageAt(conv.getLastMessageAt())
                .lastMessage(lastMsg)
                .unreadCount(unread)
                .members(memberResponses)
                .build();
    }

    private MessageResponse toMessageResponse(com.qchat.qchat.chat.entity.Message m) {
        return MessageResponse.builder()
                .id(m.getId())
                .conversationId(m.getConversation().getId())
                .senderId(m.getSender() != null ? m.getSender().getId() : null)
                .senderUsername(m.getSender() != null ? m.getSender().getUsername() : null)
                .senderDisplayName(m.getSender() != null && m.getSender().getProfile() != null
                        ? m.getSender().getProfile().getDisplayName() : null)
                .senderAvatarUrl(m.getSender() != null && m.getSender().getProfile() != null
                        ? m.getSender().getProfile().getAvatarUrl() : null)
                .content(m.isDeleted() ? null : m.getContent())
                .messageType(m.getMessageType())
                .mediaUrl(m.isDeleted() ? null : m.getMediaUrl())
                .edited(m.isEdited())
                .deleted(m.isDeleted())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }

    private ConversationMember member(Conversation conv, User user, MemberRole role) {
        return ConversationMember.builder().conversation(conv).user(user).role(role).build();
    }

    private User getUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private void assertMember(UUID conversationId, UUID userId) {
        if (!memberRepository.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId)) {
            throw new AppException(ErrorCode.DENIED_PERMISSION);
        }
    }

    private void assertAdmin(UUID conversationId, UUID userId) {
        ConversationMember m = memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.DENIED_PERMISSION));
        if (m.getRole() != MemberRole.ADMIN) throw new AppException(ErrorCode.DENIED_PERMISSION);
    }
}
