-- ============================================================
-- V4 : Add requested custom admin account
-- ============================================================
-- Email: admin@example.com
-- Password: admin123  (bcrypt, cost 12)
-- ============================================================

INSERT INTO admin_users (id, email, password_hash)
VALUES (
    gen_random_uuid(),
    'admin@example.com',
    '$2a$12$Rs/JQcbVV1wT0Vf1mFNH.uS2gSqA6E2Jdc67P4EpTivmaXSLyw8Xm'
);
