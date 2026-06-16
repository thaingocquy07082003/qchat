-- ============================================================
-- Seed: messages cho conversation 32de7e7d-2902-44a4-ae95-cc6b46f4f59b
-- user01 (ngocquy01) : 1e26bf25-3339-4e57-82a5-686f0bf86d4f
-- user02 (ngocquy02) : e552a2e8-0c3b-46f2-be5b-427bd9830748
-- ============================================================

DO $$
DECLARE
    conv_id   UUID := '32de7e7d-2902-44a4-ae95-cc6b46f4f59b';
    user01_id UUID := '1e26bf25-3339-4e57-82a5-686f0bf86d4f';
    user02_id UUID := 'e552a2e8-0c3b-46f2-be5b-427bd9830748';
    msg1_id   UUID;
    msg2_id   UUID;
BEGIN

INSERT INTO messages (id, conversation_id, sender_id, content, message_type, created_at, updated_at)
VALUES
    (gen_random_uuid(), conv_id, user01_id, 'Chào bạn! Nhóm chat thử nghiệm này hoạt động chưa?',       'TEXT', NOW() - INTERVAL '2 hours',   NOW() - INTERVAL '2 hours'),
    (gen_random_uuid(), conv_id, user02_id, 'Chào! Mình thấy được rồi, nhắn tin được bình thường.',      'TEXT', NOW() - INTERVAL '1 hour 55 minutes', NOW() - INTERVAL '1 hour 55 minutes'),
    (gen_random_uuid(), conv_id, user01_id, 'Tuyệt! Thử gửi ảnh sau nhé, giờ test text trước.',          'TEXT', NOW() - INTERVAL '1 hour 50 minutes', NOW() - INTERVAL '1 hour 50 minutes'),
    (gen_random_uuid(), conv_id, user02_id, 'Oke. Bạn đã setup Redis chưa? Pub/Sub có chạy không?',      'TEXT', NOW() - INTERVAL '1 hour 40 minutes', NOW() - INTERVAL '1 hour 40 minutes'),
    (gen_random_uuid(), conv_id, user01_id, 'Chạy rồi. WebSocket cũng kết nối ổn định, không bị drop.',  'TEXT', NOW() - INTERVAL '1 hour 30 minutes', NOW() - INTERVAL '1 hour 30 minutes'),
    (gen_random_uuid(), conv_id, user02_id, 'Ngon! Latency của bạn là bao nhiêu ms?',                    'TEXT', NOW() - INTERVAL '1 hour 20 minutes', NOW() - INTERVAL '1 hour 20 minutes'),
    (gen_random_uuid(), conv_id, user01_id, 'Khoảng 30-50ms. Chấp nhận được với local.',                 'TEXT', NOW() - INTERVAL '1 hour 10 minutes', NOW() - INTERVAL '1 hour 10 minutes'),
    (gen_random_uuid(), conv_id, user02_id, 'Production thì phải dưới 100ms mới ổn.',                    'TEXT', NOW() - INTERVAL '1 hour',            NOW() - INTERVAL '1 hour'),
    (gen_random_uuid(), conv_id, user01_id, 'Đúng rồi. Để xem thêm sau khi deploy lên server thật.',    'TEXT', NOW() - INTERVAL '50 minutes',        NOW() - INTERVAL '50 minutes'),
    (gen_random_uuid(), conv_id, user02_id, 'Ok bro. Test tiếp đi, mình ready 👍',                       'TEXT', NOW() - INTERVAL '40 minutes',        NOW() - INTERVAL '40 minutes');

-- Cập nhật last_message_at cho conversation
UPDATE conversations
SET last_message_at = NOW() - INTERVAL '40 minutes'
WHERE id = conv_id;

-- Cập nhật last_read_at cho cả 2 member
UPDATE conversation_members
SET last_read_at = NOW() - INTERVAL '40 minutes'
WHERE conversation_id = conv_id;

END $$;
