-- ============================================================
-- V3 : Clean up stub artefacts
-- ============================================================
-- 1. Drop the old Hibernate-managed stub table.
--    It was already dropped in V1 but this migration keeps the
--    intent explicit in the migration history, and is safe to
--    re-run (IF EXISTS) in case any environment missed V1.
-- ============================================================

DROP TABLE IF EXISTS location_points;

-- ============================================================
-- 2. Remove orphaned test pings.
--    Any rows in location_pings whose user_id has no matching
--    row in users were inserted during the stub-testing phase
--    (manual PowerShell curl calls, fake UUIDs, Delhi test
--    coordinates, etc.).  They are not real field data.
-- ============================================================

DELETE FROM location_pings
WHERE user_id NOT IN (SELECT id FROM users);
