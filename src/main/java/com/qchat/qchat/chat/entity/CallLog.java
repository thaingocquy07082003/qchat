package com.qchat.qchat.chat.entity;

import com.qchat.qchat.auth.entity.User;
import com.qchat.qchat.chat.enums.CallStatus;
import com.qchat.qchat.chat.enums.CallType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "call_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallLog {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caller_id", nullable = false)
    private User caller;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "call_type", columnDefinition = "call_type", nullable = false)
    @Builder.Default
    private CallType callType = CallType.VOICE;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "status", columnDefinition = "call_status", nullable = false)
    @Builder.Default
    private CallStatus status = CallStatus.ONGOING;

    @Column(name = "started_at", nullable = false, updatable = false)
    private OffsetDateTime startedAt;

    @Column(name = "answered_at")
    private OffsetDateTime answeredAt;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    @Column(name = "duration_seconds", nullable = false)
    @Builder.Default
    private int durationSeconds = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "callLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CallParticipant> participants = new ArrayList<>();

    @PrePersist
    void prePersist() {
        startedAt = OffsetDateTime.now();
        createdAt = OffsetDateTime.now();
    }
}
