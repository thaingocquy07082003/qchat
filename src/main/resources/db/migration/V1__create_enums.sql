-- ============================================================
-- V1: PostgreSQL Custom ENUM Types
-- ============================================================

-- Auth / Account
CREATE TYPE oauth_provider_type AS ENUM ('LOCAL', 'GOOGLE', 'FACEBOOK', 'GITHUB');

-- Social
CREATE TYPE friendship_status AS ENUM ('PENDING', 'ACCEPTED', 'DECLINED', 'BLOCKED');

-- Conversation & Messaging
CREATE TYPE conversation_type  AS ENUM ('DIRECT', 'GROUP');
CREATE TYPE member_role        AS ENUM ('ADMIN', 'MEMBER');
CREATE TYPE message_type       AS ENUM ('TEXT', 'IMAGE', 'VIDEO', 'FILE', 'AUDIO', 'STICKER', 'CALL_LOG', 'SYSTEM');
CREATE TYPE call_type          AS ENUM ('VOICE', 'VIDEO');
CREATE TYPE call_status        AS ENUM ('MISSED', 'ANSWERED', 'REJECTED', 'ENDED', 'ONGOING');

-- Posts
CREATE TYPE post_type          AS ENUM ('TEXT', 'IMAGE', 'VIDEO', 'REEL');
CREATE TYPE visibility_type    AS ENUM ('PUBLIC', 'FRIENDS', 'PRIVATE');
CREATE TYPE reaction_type      AS ENUM ('LIKE', 'LOVE', 'HAHA', 'WOW', 'SAD', 'ANGRY', 'CARE');

-- Media
CREATE TYPE media_type         AS ENUM ('IMAGE', 'VIDEO', 'AUDIO', 'FILE');

-- Notifications
CREATE TYPE notification_type  AS ENUM (
    'FRIEND_REQUEST',
    'FRIEND_ACCEPTED',
    'POST_LIKE',
    'POST_COMMENT',
    'COMMENT_REPLY',
    'COMMENT_LIKE',
    'MESSAGE',
    'MENTION',
    'CALL_MISSED',
    'STORY_REACTION',
    'STORY_MENTION'
);

-- Stories
CREATE TYPE story_media_type   AS ENUM ('IMAGE', 'VIDEO');
