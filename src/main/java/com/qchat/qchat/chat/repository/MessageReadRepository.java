package com.qchat.qchat.chat.repository;

import com.qchat.qchat.chat.entity.MessageRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageReadRepository extends JpaRepository<MessageRead, UUID> {

    boolean existsByMessageIdAndUserId(UUID messageId, UUID userId);
}
