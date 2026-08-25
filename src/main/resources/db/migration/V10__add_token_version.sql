-- ============================================================
-- V10 : Add token_version for server-side logout
-- ============================================================
-- JWTs no longer expire automatically. Instead, they embed
-- the user's current token_version. Logging out increments
-- the token_version in the DB, instantly invalidating all
-- previously issued tokens for that user.
-- ============================================================

ALTER TABLE users ADD COLUMN token_version INT NOT NULL DEFAULT 1;
ALTER TABLE admin_users ADD COLUMN token_version INT NOT NULL DEFAULT 1;
