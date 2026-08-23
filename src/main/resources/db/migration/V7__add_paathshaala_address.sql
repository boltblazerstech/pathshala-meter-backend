-- ============================================================
-- V7 : Add address field to paathshaalas
-- ============================================================
-- Stores the resolved reverse-geocoded address when a paathshaala
-- is created or its location is successfully updated.
-- ============================================================

ALTER TABLE paathshaalas ADD COLUMN address TEXT;
