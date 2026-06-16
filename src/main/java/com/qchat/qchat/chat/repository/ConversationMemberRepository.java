package com.qchat.qchat.chat.repository;

import com.qchat.qchat.chat.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {

    Optional<ConversationMember> findByConversationIdAndUserId(UUID conversationId, UUID userId);

    List<ConversationMember> findByConversationIdAndIsActiveTrue(UUID conversationId);

    boolean existsByConversationIdAndUserIdAndIsActiveTrue(UUID conversationId, UUID userId);

    @Query(value = """
            SELECT COUNT(*) FROM messages m
            WHERE m.conversation_id = :conversationId
              AND m.is_deleted = false
              AND m.created_at > COALESCE(
                  (SELECT cm.last_read_at FROM conversation_members cm
                   WHERE cm.conversation_id = :conversationId AND cm.user_id = :userId),
                  '1970-01-01'::timestamptz
              )
            """, nativeQuery = true)
    long countUnreadMessages(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);
}
