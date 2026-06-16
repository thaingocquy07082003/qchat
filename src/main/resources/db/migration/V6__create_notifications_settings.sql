-- ============================================================
-- V6: Notifications, User Settings & Devices
-- ============================================================

-- -------------------- notifications --------------------
CREATE TABLE notifications (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sender_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    notification_type notification_type NOT NULL,
    reference_id      UUID,            -- ID of the related entity (post, comment, message, etc.)
    reference_type    VARCHAR(50),     -- 'POST' | 'COMMENT' | 'MESSAGE' | 'FRIENDSHIP' | 'STORY'
    title             VARCHAR(255),
    body              TEXT,
    image_url         VARCHAR(500),
    is_read           BOOLEAN NOT NULL DEFAULT FALSE,
    read_at           TIMESTAMP WITH TIME ZONE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- -------------------- user_settings --------------------
CREATE TABLE user_settings (
    id                           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                      UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    -- Notification toggles
    notif_messages               BOOLEAN NOT NULL DEFAULT TRUE,
    notif_friend_requests        BOOLEAN NOT NULL DEFAULT TRUE,
    notif_post_likes             BOOLEAN NOT NULL DEFAULT TRUE,
    notif_comments               BOOLEAN NOT NULL DEFAULT TRUE,
    notif_calls                  BOOLEAN NOT NULL DEFAULT TRUE,
    notif_mentions               BOOLEAN NOT NULL DEFAULT TRUE,
    notif_story_reactions        BOOLEAN NOT NULL DEFAULT TRUE,
    -- Privacy
    who_can_send_message         VARCHAR(20) NOT NULL DEFAULT 'FRIENDS',    -- EVERYONE | FRIENDS | NOBODY
    who_can_see_posts            VARCHAR(20) NOT NULL DEFAULT 'FRIENDS',
    who_can_send_friend_request  VARCHAR(20) NOT NULL DEFAULT 'EVERYONE',
    who_can_see_friends_list     VARCHAR(20) NOT NULL DEFAULT 'FRIENDS',
    -- UI
    theme                        VARCHAR(10) NOT NULL DEFAULT 'LIGHT',
    language                     VARCHAR(10) NOT NULL DEFAULT 'vi',
    created_at                   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at                   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- -------------------- user_devices --------------------
-- Stores FCM / APNs push tokens per device.
CREATE TABLE user_devices (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_token VARCHAR(512) NOT NULL,
    device_type  VARCHAR(20)  NOT NULL,   -- ANDROID | IOS | WEB
    device_name  VARCHAR(100),
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT user_devices_unique UNIQUE (user_id, device_token)
);
