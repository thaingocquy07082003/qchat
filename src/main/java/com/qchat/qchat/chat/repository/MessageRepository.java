package com.qchat.qchat.chat.repository;

import com.qchat.qchat.chat.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("""
            SELECT m FROM Message m
            LEFT JOIN FETCH m.sender s
            LEFT JOIN FETCH s.profile
            WHERE m.conversation.id = :conversationId AND m.isDeleted = false
            ORDER BY m.createdAt DESC
            """)
    Page<Message> findByConversationId(
            @Param("conversationId") UUID conversationId,
            Pageable pageable);

    @Query("""
            SELECT m FROM Message m
            LEFT JOIN FETCH m.sender s
            LEFT JOIN FETCH s.profile
            LEFT JOIN FETCH m.replyTo r
            LEFT JOIN FETCH r.sender
            WHERE m.id = :id
            """)
    Optional<Message> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
            SELECT m FROM Message m
            LEFT JOIN FETCH m.sender s
            LEFT JOIN FETCH s.profile
            WHERE m.conversation.id = :conversationId AND m.isDeleted = false
            ORDER BY m.createdAt DESC
            LIMIT 1
            """)
    Optional<Message> findLastMessage(@Param("conversationId") UUID conversationId);
}
