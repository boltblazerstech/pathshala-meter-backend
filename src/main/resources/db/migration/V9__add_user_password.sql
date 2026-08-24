-- ============================================================
-- V9 : Add per-user plaintext password column
-- ============================================================
-- Replaces the shared field_app_shared_password in system_config.
-- Each user (supervisor/teacher) now has their own password.
-- Stored in plaintext per project requirements.
-- Existing users get a random 6-digit password so they can
-- still log in immediately.
-- ============================================================

ALTER TABLE users ADD COLUMN password VARCHAR(255);

-- Seed existing users with random 6-digit passwords
UPDATE users SET password = lpad(floor(random() * 1000000)::text, 6, '0');
