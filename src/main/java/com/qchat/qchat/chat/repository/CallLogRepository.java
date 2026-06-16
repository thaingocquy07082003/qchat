package com.qchat.qchat.chat.repository;

import com.qchat.qchat.chat.entity.CallLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CallLogRepository extends JpaRepository<CallLog, UUID> {

    @Query("SELECT c FROM CallLog c WHERE c.conversation.id = :conversationId ORDER BY c.startedAt DESC")
    List<CallLog> findByConversationId(@Param("conversationId") UUID conversationId, Pageable pageable);
}
