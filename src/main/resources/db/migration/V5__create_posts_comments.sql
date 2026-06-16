-- ============================================================
-- V5: Posts, Media, Reactions, Comments & Hashtags
-- ============================================================

-- -------------------- posts --------------------
CREATE TABLE posts (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content        TEXT,
    post_type      post_type       NOT NULL DEFAULT 'TEXT',
    visibility     visibility_type NOT NULL DEFAULT 'FRIENDS',
    location       VARCHAR(255),
    feeling        VARCHAR(100),
    is_deleted     BOOLEAN NOT NULL DEFAULT FALSE,
    like_count     INTEGER NOT NULL DEFAULT 0,
    comment_count  INTEGER NOT NULL DEFAULT 0,
    share_count    INTEGER NOT NULL DEFAULT 0,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- -------------------- post_media --------------------
CREATE TABLE post_media (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id          UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    media_type       media_type  NOT NULL,
    url              VARCHAR(500) NOT NULL,
    thumbnail_url    VARCHAR(500),
    alt_text         VARCHAR(255),
    width            INTEGER,
    height           INTEGER,
    duration_seconds INTEGER,
    order_index      SMALLINT NOT NULL DEFAULT 0,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- -------------------- post_reactions --------------------
CREATE TABLE post_reactions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id       UUID NOT NULL REFERENCES posts(id)  ON DELETE CASCADE,
    user_id       UUID NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    reaction_type reaction_type NOT NULL DEFAULT 'LIKE',
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT post_reactions_unique UNIQUE (post_id, user_id)
);

-- -------------------- post_shares --------------------
CREATE TABLE post_shares (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id     UUID NOT NULL REFERENCES posts(id)  ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    content     TEXT,
    visibility  visibility_type NOT NULL DEFAULT 'FRIENDS',
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- -------------------- post_tags --------------------
-- Tag other users in a post.
CREATE TABLE post_tags (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id        UUID NOT NULL REFERENCES posts(id)  ON DELETE CASCADE,
    tagged_user_id UUID NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT post_tags_unique UNIQUE (post_id, tagged_user_id)
);

-- -------------------- hashtags --------------------
CREATE TABLE hashtags (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(100) UNIQUE NOT NULL,
    post_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- -------------------- post_hashtags --------------------
CREATE TABLE post_hashtags (
    post_id    UUID NOT NULL REFERENCES posts(id)    ON DELETE CASCADE,
    hashtag_id UUID NOT NULL REFERENCES hashtags(id) ON DELETE CASCADE,
    PRIMARY KEY (post_id, hashtag_id)
);

-- -------------------- comments --------------------
-- Supports nested replies (1 level deep recommended; parent_comment_id is NULL for top-level).
CREATE TABLE comments (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id           UUID NOT NULL REFERENCES posts(id)    ON DELETE CASCADE,
    author_id         UUID NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    parent_comment_id UUID REFERENCES comments(id)          ON DELETE CASCADE,
    content           TEXT NOT NULL,
    media_url         VARCHAR(500),
    media_type        media_type,
    like_count        INTEGER NOT NULL DEFAULT 0,
    reply_count       INTEGER NOT NULL DEFAULT 0,
    is_deleted        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- -------------------- comment_reactions --------------------
CREATE TABLE comment_reactions (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    comment_id    UUID NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    user_id       UUID NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    reaction_type reaction_type NOT NULL DEFAULT 'LIKE',
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT comment_reactions_unique UNIQUE (comment_id, user_id)
);
