package com.qchat.qchat.chat.service;

import com.qchat.qchat.auth.entity.User;
import com.qchat.qchat.auth.repository.UserRepository;
import com.qchat.qchat.chat.dto.request.InitiateCallRequest;
import com.qchat.qchat.chat.dto.request.WebRtcSignalRequest;
import com.qchat.qchat.chat.dto.response.CallEvent;
import com.qchat.qchat.chat.dto.response.CallLogResponse;
import com.qchat.qchat.chat.entity.CallLog;
import com.qchat.qchat.chat.entity.CallParticipant;
import com.qchat.qchat.chat.entity.ConversationMember;
import com.qchat.qchat.chat.enums.CallStatus;
import com.qchat.qchat.chat.messaging.RedisMessagePublisher;
import com.qchat.qchat.chat.repository.CallLogRepository;
import com.qchat.qchat.chat.repository.CallParticipantRepository;
import com.qchat.qchat.chat.repository.ConversationMemberRepository;
import com.qchat.qchat.exception.AppException;
import com.qchat.qchat.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CallServiceImpl implements CallService {

    private final CallLogRepository          callLogRepository;
    private final CallParticipantRepository  participantRepository;
    private final ConversationMemberRepository memberRepository;
    private final UserRepository             userRepository;
    private final RedisMessagePublisher      publisher;
    private final SimpMessagingTemplate      messagingTemplate;

    @Override
    public void initiateCall(UUID callerId, InitiateCallRequest request) {
        ConversationMember member = memberRepository
                .findByConversationIdAndUserId(request.getConversationId(), callerId)
                .filter(ConversationMember::isActive)
                .orElseThrow(() -> new AppException(ErrorCode.DENIED_PERMISSION));

        User caller           = member.getUser();
        var  conversation     = member.getConversation();

        CallLog callLog = callLogRepository.save(CallLog.builder()
                .conversation(conversation)
                .caller(caller)
                .callType(request.getCallType())
                .status(CallStatus.ONGOING)
                .build());

        participantRepository.save(CallParticipant.builder()
                .callLog(callLog)
                .user(caller)
                .joinedAt(OffsetDateTime.now())
                .build());

        publisher.publish(conversation.getId(), "CALL_EVENT",
                buildEvent("INCOMING_CALL", callLog, caller, null));

        log.info("[CALL] INITIATE | callId={} | caller={} | conv={} | type={}",
                callLog.getId(), callerId, conversation.getId(), request.getCallType());
    }

    @Override
    public void acceptCall(UUID userId, UUID callLogId) {
        CallLog callLog = getCallLog(callLogId);

        ConversationMember member = memberRepository
                .findByConversationIdAndUserId(callLog.getConversation().getId(), userId)
                .filter(ConversationMember::isActive)
                .orElseThrow(() -> new AppException(ErrorCode.DENIED_PERMISSION));

        User user = member.getUser();

        if (!participantRepository.existsByCallLog_IdAndUser_Id(callLogId, userId)) {
            participantRepository.save(CallParticipant.builder()
                    .callLog(callLog)
                    .user(user)
                    .joinedAt(OffsetDateTime.now())
                    .build());
        }

        if (callLog.getAnsweredAt() == null) {
            callLog.setAnsweredAt(OffsetDateTime.now());
            callLog.setStatus(CallStatus.ANSWERED);
        }

        publisher.publish(callLog.getConversation().getId(), "CALL_EVENT",
                buildEvent("CALL_ACCEPTED", callLog, user, null));

        log.info("[CALL] ACCEPTED | callId={} | userId={}", callLogId, userId);
    }

    @Override
    public void rejectCall(UUID userId, UUID callLogId) {
        CallLog callLog = getCallLog(callLogId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        publisher.publish(callLog.getConversation().getId(), "CALL_EVENT",
                buildEvent("CALL_REJECTED", callLog, user, null));

        log.info("[CALL] REJECTED | callId={} | userId={}", callLogId, userId);
    }

    @Override
    public void endCall(UUID userId, UUID callLogId) {
        CallLog callLog = getCallLog(callLogId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        participantRepository.findByCallLog_IdAndUser_Id(callLogId, userId)
                .ifPresent(p -> p.setLeftAt(OffsetDateTime.now()));

        boolean noActiveParticipants = participantRepository.findByCallLog_Id(callLogId)
                .stream().allMatch(p -> p.getLeftAt() != null);

        if (noActiveParticipants) {
            OffsetDateTime now = OffsetDateTime.now();
            callLog.setEndedAt(now);
            if (callLog.getAnsweredAt() != null) {
                callLog.setDurationSeconds(
                        (int) ChronoUnit.SECONDS.between(callLog.getAnsweredAt(), now));
                callLog.setStatus(CallStatus.ENDED);
            } else {
                callLog.setStatus(CallStatus.MISSED);
            }
        }

        String eventType = noActiveParticipants ? "CALL_ENDED" : "PARTICIPANT_LEFT";
        publisher.publish(callLog.getConversation().getId(), "CALL_EVENT",
                buildEvent(eventType, callLog, user, null));

        log.info("[CALL] {} | callId={} | userId={}", eventType, callLogId, userId);
    }

    @Override
    public void relaySignal(UUID fromUserId, WebRtcSignalRequest request) {
        if (request.getToUserId() == null || request.getSignalType() == null || request.getPayload() == null) {
            log.warn("[CALL] SIGNAL dropped — missing required field | from={} | toUserId={} | signalType={}",
                    fromUserId, request.getToUserId(), request.getSignalType());
            return;
        }

        User from = userRepository.findById(fromUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User to = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        CallEvent event = CallEvent.builder()
                .type(request.getSignalType())
                .callLogId(request.getCallLogId())
                .fromUserId(fromUserId)
                .senderUsername(from.getUsername())
                .payload(request.getPayload())
                .timestamp(OffsetDateTime.now())
                .build();

        // Route directly to the target peer's private queue
        messagingTemplate.convertAndSendToUser(to.getUsername(), "/queue/call.signal", event);

        log.debug("[CALL] SIGNAL {} | from={} | to={}", request.getSignalType(), fromUserId, request.getToUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CallLogResponse> getCallHistory(UUID conversationId, UUID userId) {
        if (!memberRepository.existsByConversationIdAndUserIdAndIsActiveTrue(conversationId, userId)) {
            throw new AppException(ErrorCode.DENIED_PERMISSION);
        }
        return callLogRepository
                .findByConversationId(conversationId, PageRequest.of(0, 20))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private CallLog getCallLog(UUID callLogId) {
        return callLogRepository.findById(callLogId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private CallEvent buildEvent(String type, CallLog callLog, User user, String payload) {
        return CallEvent.builder()
                .type(type)
                .callLogId(callLog.getId())
                .conversationId(callLog.getConversation().getId())
                .fromUserId(user.getId())
                .senderUsername(user.getUsername())
                .senderDisplayName(user.getProfile() != null ? user.getProfile().getDisplayName() : null)
                .senderAvatarUrl(user.getProfile() != null ? user.getProfile().getAvatarUrl() : null)
                .callType(callLog.getCallType())
                .durationSeconds(callLog.getDurationSeconds())
                .payload(payload)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    private CallLogResponse toResponse(CallLog callLog) {
        User caller = callLog.getCaller();
        List<CallLogResponse.ParticipantInfo> participants = callLog.getParticipants().stream()
                .map(p -> CallLogResponse.ParticipantInfo.builder()
                        .userId(p.getUser().getId())
                        .username(p.getUser().getUsername())
                        .displayName(p.getUser().getProfile() != null
                                ? p.getUser().getProfile().getDisplayName() : null)
                        .joinedAt(p.getJoinedAt())
                        .leftAt(p.getLeftAt())
                        .build())
                .collect(Collectors.toList());

        return CallLogResponse.builder()
                .id(callLog.getId())
                .conversationId(callLog.getConversation().getId())
                .callerId(caller.getId())
                .callerUsername(caller.getUsername())
                .callerDisplayName(caller.getProfile() != null ? caller.getProfile().getDisplayName() : null)
                .callerAvatarUrl(caller.getProfile() != null ? caller.getProfile().getAvatarUrl() : null)
                .callType(callLog.getCallType())
                .status(callLog.getStatus())
                .startedAt(callLog.getStartedAt())
                .answeredAt(callLog.getAnsweredAt())
                .endedAt(callLog.getEndedAt())
                .durationSeconds(callLog.getDurationSeconds())
                .participants(participants)
                .build();
    }
}
