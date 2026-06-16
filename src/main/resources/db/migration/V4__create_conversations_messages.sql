-- ============================================================
-- V4: Conversations, Messages & Calls
-- ============================================================

-- -------------------- conversations --------------------
CREATE TABLE conversations (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type             conversation_type NOT NULL DEFAULT 'DIRECT',
    name             VARCHAR(100),            -- group chat name
    avatar_url       VARCHAR(500),            -- group chat avatar
    description      TEXT,
    created_by       UUID REFERENCES users(id) ON DELETE SET NULL,
    last_message_at  TIMESTAMP WITH TIME ZONE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- -------------------- conversation_members --------------------
CREATE TABLE conversation_members (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id  UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    user_id          UUID NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    role             member_role NOT NULL DEFAULT 'MEMBER',
    nickname         VARCHAR(100),            -- custom nickname inside conversation
    is_muted         BOOLEAN NOT NULL DEFAULT FALSE,
    muted_until      TIMESTAMP WITH TIME ZONE,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    last_read_at     TIMESTAMP WITH TIME ZONE,
    joined_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    left_at          TIMESTAMP WITH TIME ZONE,
    CONSTRAINT conv_members_unique UNIQUE (conversation_id, user_id)
);

-- -------------------- messages --------------------
CREATE TABLE messages (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id  UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id        UUID REFERENCES users(id) ON DELETE SET NULL,
    content          TEXT,
    message_type     message_type NOT NULL DEFAULT 'TEXT',
    media_url        VARCHAR(500),
    media_thumbnail  VARCHAR(500),
    media_size_bytes BIGINT,
    reply_to_id      UUID REFERENCES messages(id) ON DELETE SET NULL,
    is_edited        BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted       BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_for_all  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- -------------------- message_reads --------------------
-- Tracks per-user read receipt for each message.
CREATE TABLE message_reads (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id  UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    read_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT msg_reads_unique UNIQUE (message_id, user_id)
);

-- -------------------- message_reactions --------------------
CREATE TABLE message_reactions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id  UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    emoji       VARCHAR(10) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT msg_reactions_unique UNIQUE (message_id, user_id)
);

-- -------------------- call_logs --------------------
CREATE TABLE call_logs (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id  UUID REFERENCES conversations(id) ON DELETE SET NULL,
    caller_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    call_type        call_type   NOT NULL DEFAULT 'VOICE',
    status           call_status NOT NULL DEFAULT 'MISSED',
    started_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    answered_at      TIMESTAMP WITH TIME ZONE,
    ended_at         TIMESTAMP WITH TIME ZONE,
    duration_seconds INTEGER NOT NULL DEFAULT 0,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- -------------------- call_participants --------------------
CREATE TABLE call_participants (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    call_log_id  UUID NOT NULL REFERENCES call_logs(id) ON DELETE CASCADE,
    user_id      UUID NOT NULL REFERENCES users(id)     ON DELETE CASCADE,
    joined_at    TIMESTAMP WITH TIME ZONE,
    left_at      TIMESTAMP WITH TIME ZONE,
    CONSTRAINT call_participants_unique UNIQUE (call_log_id, user_id)
);
