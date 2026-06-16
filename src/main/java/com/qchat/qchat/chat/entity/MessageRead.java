package com.qchat.qchat.chat.entity;

import com.qchat.qchat.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "message_reads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRead {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "read_at", nullable = false)
    @Builder.Default
    private OffsetDateTime readAt = OffsetDateTime.now();

    @PrePersist
    void prePersist() {
        if (readAt == null) readAt = OffsetDateTime.now();
    }
}
