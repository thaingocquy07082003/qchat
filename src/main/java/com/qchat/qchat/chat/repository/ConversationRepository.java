package com.qchat.qchat.chat.repository;

import com.qchat.qchat.chat.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("""
            SELECT c FROM Conversation c
            JOIN c.members m
            WHERE m.user.id = :userId AND m.isActive = true
            ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC
            """)
    List<Conversation> findAllByMemberUserId(@Param("userId") UUID userId);

    @Query("""
            SELECT c FROM Conversation c
            WHERE c.type = com.qchat.qchat.chat.enums.ConversationType.DIRECT
            AND EXISTS (
                SELECT m FROM ConversationMember m
                WHERE m.conversation = c AND m.user.id = :user1Id AND m.isActive = true
            )
            AND EXISTS (
                SELECT m FROM ConversationMember m
                WHERE m.conversation = c AND m.user.id = :user2Id AND m.isActive = true
            )
            """)
    Optional<Conversation> findDirectConversation(
            @Param("user1Id") UUID user1Id,
            @Param("user2Id") UUID user2Id);

    @Modifying
    @Query("UPDATE Conversation c SET c.lastMessageAt = :ts WHERE c.id = :id")
    void updateLastMessageAt(@Param("id") UUID id, @Param("ts") OffsetDateTime ts);
}
