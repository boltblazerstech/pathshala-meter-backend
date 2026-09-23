-- ============================================================
-- V13 : Add home location and paathshaala fields
-- ============================================================

-- Add home location to users
ALTER TABLE users ADD COLUMN home_lat DOUBLE PRECISION;
ALTER TABLE users ADD COLUMN home_lng DOUBLE PRECISION;
ALTER TABLE users ADD COLUMN home_map_link TEXT;
ALTER TABLE users ADD COLUMN home_confidence VARCHAR(10) 
    CHECK (home_confidence IN ('parsed', 'fallback', 'manual', 'unresolved'));

-- Add fields to paathshaalas
ALTER TABLE paathshaalas ADD COLUMN supervisor_id UUID REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE paathshaalas ADD COLUMN opening_time TIME;
ALTER TABLE paathshaalas ADD COLUMN closing_time TIME;

-- Seed system configuration defaults
INSERT INTO system_config (key, value) VALUES 
('fetch_interval_minutes', '10'),
('pre_buffer_minutes', '45'),
('post_buffer_minutes', '45'),
('radius_leave_home_meters', '200'),
('radius_reach_school_meters', '200'),
('radius_leave_school_meters', '200'),
('radius_reach_home_meters', '200');
