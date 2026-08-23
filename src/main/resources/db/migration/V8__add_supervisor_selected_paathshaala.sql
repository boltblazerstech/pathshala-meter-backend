-- ============================================================
-- V8 : Add selected_paathshaala_id to users for supervisors
-- ============================================================
-- Supervisors don't have a fixed paathshaala assignment, but
-- they can select one to monitor. This tracks their currently
-- selected paathshaala.
-- ============================================================

ALTER TABLE users 
ADD COLUMN selected_paathshaala_id UUID REFERENCES paathshaalas(id) ON DELETE SET NULL;
