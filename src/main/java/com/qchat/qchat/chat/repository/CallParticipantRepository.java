package com.qchat.qchat.chat.repository;

import com.qchat.qchat.chat.entity.CallParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CallParticipantRepository extends JpaRepository<CallParticipant, UUID> {

    Optional<CallParticipant> findByCallLog_IdAndUser_Id(UUID callLogId, UUID userId);

    List<CallParticipant> findByCallLog_Id(UUID callLogId);

    boolean existsByCallLog_IdAndUser_Id(UUID callLogId, UUID userId);
}
