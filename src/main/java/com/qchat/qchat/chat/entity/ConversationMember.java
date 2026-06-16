package com.qchat.qchat.chat.entity;

import com.qchat.qchat.auth.entity.User;
import com.qchat.qchat.chat.enums.MemberRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversation_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMember {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", columnDefinition = "member_role", nullable = false)
    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;

    @Column(length = 100)
    private String nickname;

    @Column(name = "is_muted", nullable = false)
    @Builder.Default
    private boolean isMuted = false;

    @Column(name = "muted_until")
    private OffsetDateTime mutedUntil;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "last_read_at")
    private OffsetDateTime lastReadAt;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "left_at")
    private OffsetDateTime leftAt;

    @PrePersist
    void prePersist() {
        joinedAt = OffsetDateTime.now();
    }
}
