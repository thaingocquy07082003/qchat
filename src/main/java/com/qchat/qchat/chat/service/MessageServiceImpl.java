package com.qchat.qchat.chat.service;

import com.qchat.qchat.auth.entity.User;
import com.qchat.qchat.auth.repository.UserRepository;
import com.qchat.qchat.chat.dto.request.ReadReceiptRequest;
import com.qchat.qchat.chat.dto.request.SendMessageRequest;
import com.qchat.qchat.chat.dto.request.TypingRequest;
import com.qchat.qchat.chat.dto.response.MessageResponse;
import com.qchat.qchat.chat.dto.response.ReadReceiptEvent;
import com.qchat.qchat.chat.dto.response.TypingEvent;
import com.qchat.qchat.chat.entity.Conversation;
import com.qchat.qchat.chat.entity.ConversationMember;
import com.qchat.qchat.chat.entity.Message;
import com.qchat.qchat.chat.entity.MessageRead;
import com.qchat.qchat.chat.messaging.RedisMessagePublisher;
import com.qchat.qchat.chat.repository.ConversationMemberRepository;
import com.qchat.qchat.chat.repository.ConversationRepository;
import com.qchat.qchat.chat.repository.MessageReadRepository;
import com.qchat.qchat.chat.repository.MessageRepository;
import com.qchat.qchat.exception.AppException;
import com.qchat.qchat.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository          messageRepository;
    private final ConversationRepository     conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageReadRepository      messageReadRepository;
    private final UserRepository             userRepository;
    private final RedisMessagePublisher      publisher;

    @Override
    public MessageResponse sendMessage(UUID senderId, SendMessageRequest request) {
        ConversationMember member = memberRepository
                .findByConversationIdAndUserId(request.getConversationId(), senderId)
                .filter(ConversationMember::isActive)
                .orElseThrow(() -> new AppException(ErrorCode.DENIED_PERMISSION));

        Conversation conv   = member.getConversation();
        User         sender = member.getUser();

        Message replyTo = null;
        if (request.getReplyToId() != null) {
            replyTo = messageRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        Message message = Message.builder()
                .conversation(conv)
                .sender(sender)
                .content(request.getContent())
                .messageType(request.getMessageType())
                .mediaUrl(request.getMediaUrl())
                .mediaThumbnail(request.getMediaThumbnail())
                .mediaSizeBytes(request.getMediaSizeBytes())
                .replyTo(replyTo)
                .build();

        message = messageRepository.save(message);
        conversationRepository.updateLastMessageAt(conv.getId(), OffsetDateTime.now());

        MessageResponse response = toResponse(message);
        publisher.publish(conv.getId(), "NEW_MESSAGE", response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(UUID conversationId, UUID requestingUserId, int page, int size) {
        if (!memberRepository.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, requestingUserId)) {
            throw new AppException(ErrorCode.DENIED_PERMISSION);
        }
        return messageRepository
                .findByConversationId(conversationId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Override
    public void markAsRead(UUID userId, ReadReceiptRequest request) {
        if (!memberRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
                request.getConversationId(), userId)) {
            throw new AppException(ErrorCode.DENIED_PERMISSION);
        }

        Message lastMsg = messageRepository.findById(request.getLastReadMessageId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!messageReadRepository.existsByMessageIdAndUserId(lastMsg.getId(), userId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            messageReadRepository.save(MessageRead.builder().message(lastMsg).user(user).build());
        }

        memberRepository.findByConversationIdAndUserId(request.getConversationId(), userId)
                .ifPresent(m -> m.setLastReadAt(OffsetDateTime.now()));

        ReadReceiptEvent event = ReadReceiptEvent.builder()
                .conversationId(request.getConversationId())
                .userId(userId)
                .lastReadMessageId(lastMsg.getId())
                .readAt(OffsetDateTime.now())
                .build();

        publisher.publish(request.getConversationId(), "READ_RECEIPT", event);
    }

    @Override
    public void sendTypingEvent(UUID userId, TypingRequest request) {
        if (!memberRepository.existsByConversationIdAndUserIdAndIsActiveTrue(
                request.getConversationId(), userId)) {
            throw new AppException(ErrorCode.DENIED_PERMISSION);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        TypingEvent event = TypingEvent.builder()
                .conversationId(request.getConversationId())
                .userId(userId)
                .username(user.getUsername())
                .typing(request.isTyping())
                .build();

        publisher.publish(request.getConversationId(), "TYPING", event);
    }

    // ── mapping ──────────────────────────────────────────────────────────────

    private MessageResponse toResponse(Message m) {
        MessageResponse.ReplyPreview replyPreview = null;
        if (m.getReplyTo() != null) {
            Message r = m.getReplyTo();
            replyPreview = MessageResponse.ReplyPreview.builder()
                    .id(r.getId())
                    .senderId(r.getSender() != null ? r.getSender().getId() : null)
                    .senderUsername(r.getSender() != null ? r.getSender().getUsername() : null)
                    .content(r.isDeleted() ? null : r.getContent())
                    .messageType(r.getMessageType())
                    .build();
        }

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
                .mediaThumbnail(m.isDeleted() ? null : m.getMediaThumbnail())
                .mediaSizeBytes(m.getMediaSizeBytes())
                .replyTo(replyPreview)
                .edited(m.isEdited())
                .deleted(m.isDeleted())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
