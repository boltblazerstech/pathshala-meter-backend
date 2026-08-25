-- ============================================================
-- V12 : Add fcm_token for push notifications
-- ============================================================
ALTER TABLE users ADD COLUMN fcm_token VARCHAR(255);
