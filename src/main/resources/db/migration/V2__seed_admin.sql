-- ============================================================
-- V2 : Seed initial admin user
-- ============================================================
-- Password: Admin@Pathshala123  (bcrypt, cost 12)
-- ⚠️  CHANGE THIS before going to production by running a new
--     migration (V3) that UPDATE admin_users SET password_hash
--     to a fresh bcrypt hash of your real admin password.
-- ============================================================

INSERT INTO admin_users (id, email, password_hash)
VALUES (
    gen_random_uuid(),
    'admin@pathshala-meter.in',
    '$2a$12$hK3Lm9NpQ7WxYvT2sRzG4eD8JcFbViU6oX0EAkMnPwCqZtSfIdOy.'
);
