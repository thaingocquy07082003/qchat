-- ============================================================
-- V7: Stories (24-hour ephemeral content)
-- ============================================================

-- -------------------- stories --------------------
CREATE TABLE stories (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_url        VARCHAR(500) NOT NULL,
    media_type       story_media_type NOT NULL DEFAULT 'IMAGE',
    thumbnail_url    VARCHAR(500),
    caption          TEXT,
    background_color VARCHAR(7),          -- hex color for text-only stories
    duration_seconds INTEGER NOT NULL DEFAULT 5,
    view_count       INTEGER NOT NULL DEFAULT 0,
    expires_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() + INTERVAL '24 hours'),
    is_archived      BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- -------------------- story_views --------------------
CREATE TABLE story_views (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    story_id    UUID NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
    viewer_id   UUID NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    viewed_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT story_views_unique UNIQUE (story_id, viewer_id)
);

-- -------------------- story_reactions --------------------
CREATE TABLE story_reactions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    story_id    UUID NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    emoji       VARCHAR(10) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT story_reactions_unique UNIQUE (story_id, user_id)
);
