-- ============================================================
-- V3: Social Connections (Friendships, Follows, Blocks)
-- ============================================================

-- -------------------- friendships --------------------
-- One row per pair; requester always has the lower UUID to avoid duplicates.
-- Application layer ensures requester_id < addressee_id or vice-versa consistently.
CREATE TABLE friendships (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    addressee_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status        friendship_status NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT friendships_no_self   CHECK (requester_id != addressee_id),
    CONSTRAINT friendships_unique    UNIQUE (requester_id, addressee_id)
);

-- -------------------- follows --------------------
-- Unidirectional follow (for public/page-style content).
CREATE TABLE follows (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT follows_no_self CHECK (follower_id != following_id),
    CONSTRAINT follows_unique  UNIQUE (follower_id, following_id)
);

-- -------------------- user_blocks --------------------
CREATE TABLE user_blocks (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    blocker_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    blocked_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason      VARCHAR(255),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT blocks_no_self CHECK (blocker_id != blocked_id),
    CONSTRAINT blocks_unique  UNIQUE (blocker_id, blocked_id)
);
