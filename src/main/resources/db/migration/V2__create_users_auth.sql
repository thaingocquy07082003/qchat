-- ============================================================
-- V2: Users & Authentication
-- ============================================================

-- -------------------- users (credentials / account status) --------------------
CREATE TABLE users (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username         VARCHAR(50)  UNIQUE NOT NULL,
    email            VARCHAR(255) UNIQUE,
    phone_number     VARCHAR(20)  UNIQUE,
    password_hash    VARCHAR(255),
    oauth_provider   oauth_provider_type NOT NULL DEFAULT 'LOCAL',
    oauth_id         VARCHAR(255),
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified   BOOLEAN NOT NULL DEFAULT FALSE,
    is_phone_verified   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT users_email_or_phone CHECK (
        email IS NOT NULL OR phone_number IS NOT NULL OR oauth_provider != 'LOCAL'
    )
);

-- -------------------- user_profiles (personal / display info) --------------------
CREATE TABLE user_profiles (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    display_name  VARCHAR(100) NOT NULL,
    avatar_url    VARCHAR(500),
    cover_url     VARCHAR(500),
    bio           TEXT,
    date_of_birth DATE,
    gender        VARCHAR(10),
    last_seen_at  TIMESTAMP WITH TIME ZONE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- -------------------- refresh_tokens --------------------
-- Stores hashed refresh tokens. AccessTokens are stateless JWT (validated via secret).
-- Blacklisted access tokens are stored in Redis with TTL = remaining expiry.
CREATE TABLE refresh_tokens (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash   VARCHAR(64) NOT NULL UNIQUE,   -- SHA-256 hex of the raw token
    expires_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    is_revoked   BOOLEAN NOT NULL DEFAULT FALSE,
    device_info  VARCHAR(255),
    ip_address   INET,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- -------------------- email_verifications --------------------
CREATE TABLE email_verifications (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    is_used     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- -------------------- password_reset_tokens --------------------
CREATE TABLE password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    is_used     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
