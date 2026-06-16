-- ============================================================
-- V8: Performance Indexes
-- ============================================================

-- -------------------- users --------------------
CREATE INDEX idx_users_email          ON users (email)          WHERE email IS NOT NULL;
CREATE INDEX idx_users_phone          ON users (phone_number)   WHERE phone_number IS NOT NULL;
CREATE INDEX idx_users_oauth          ON users (oauth_provider, oauth_id) WHERE oauth_id IS NOT NULL;
CREATE INDEX idx_users_last_seen      ON user_profiles (last_seen_at DESC);

-- -------------------- refresh_tokens --------------------
CREATE INDEX idx_rt_user_id           ON refresh_tokens (user_id);
CREATE INDEX idx_rt_expires_revoked   ON refresh_tokens (expires_at, is_revoked);

-- -------------------- friendships --------------------
CREATE INDEX idx_fs_requester         ON friendships (requester_id, status);
CREATE INDEX idx_fs_addressee         ON friendships (addressee_id, status);

-- -------------------- follows --------------------
CREATE INDEX idx_follows_follower     ON follows (follower_id);
CREATE INDEX idx_follows_following    ON follows (following_id);

-- -------------------- user_blocks --------------------
CREATE INDEX idx_blocks_blocker       ON user_blocks (blocker_id);
CREATE INDEX idx_blocks_blocked       ON user_blocks (blocked_id);

-- -------------------- conversations --------------------
CREATE INDEX idx_conv_last_msg        ON conversations (last_message_at DESC);

-- -------------------- conversation_members --------------------
CREATE INDEX idx_cm_user_active       ON conversation_members (user_id) WHERE is_active = TRUE;
CREATE INDEX idx_cm_conv_active       ON conversation_members (conversation_id) WHERE is_active = TRUE;

-- -------------------- messages --------------------
CREATE INDEX idx_msg_conv_created     ON messages (conversation_id, created_at DESC);
CREATE INDEX idx_msg_sender           ON messages (sender_id);
CREATE INDEX idx_msg_reply_to         ON messages (reply_to_id) WHERE reply_to_id IS NOT NULL;

-- -------------------- message_reads --------------------
CREATE INDEX idx_mr_user_msg          ON message_reads (user_id, message_id);

-- -------------------- call_logs --------------------
CREATE INDEX idx_call_caller          ON call_logs (caller_id, created_at DESC);
CREATE INDEX idx_call_conv            ON call_logs (conversation_id) WHERE conversation_id IS NOT NULL;

-- -------------------- posts --------------------
CREATE INDEX idx_posts_author_created ON posts (author_id, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_posts_visibility     ON posts (visibility, created_at DESC) WHERE is_deleted = FALSE;

-- -------------------- post_reactions --------------------
CREATE INDEX idx_pr_post              ON post_reactions (post_id);
CREATE INDEX idx_pr_user              ON post_reactions (user_id);

-- -------------------- post_tags --------------------
CREATE INDEX idx_pt_tagged_user       ON post_tags (tagged_user_id);

-- -------------------- hashtags --------------------
CREATE INDEX idx_hashtags_name        ON hashtags (name);

-- -------------------- post_hashtags --------------------
CREATE INDEX idx_ph_hashtag           ON post_hashtags (hashtag_id);

-- -------------------- comments --------------------
CREATE INDEX idx_comments_post        ON comments (post_id, created_at ASC)  WHERE is_deleted = FALSE;
CREATE INDEX idx_comments_parent      ON comments (parent_comment_id) WHERE parent_comment_id IS NOT NULL;
CREATE INDEX idx_comments_author      ON comments (author_id);

-- -------------------- comment_reactions --------------------
CREATE INDEX idx_cr_comment           ON comment_reactions (comment_id);

-- -------------------- notifications --------------------
CREATE INDEX idx_notif_recipient      ON notifications (recipient_id, created_at DESC);
CREATE INDEX idx_notif_unread         ON notifications (recipient_id, is_read) WHERE is_read = FALSE;

-- -------------------- stories --------------------
CREATE INDEX idx_stories_author       ON stories (author_id, created_at DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_stories_active       ON stories (expires_at)                 WHERE is_deleted = FALSE AND is_archived = FALSE;

-- -------------------- story_views --------------------
CREATE INDEX idx_sv_story             ON story_views (story_id);
CREATE INDEX idx_sv_viewer            ON story_views (viewer_id);
