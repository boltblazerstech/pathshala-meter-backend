-- ============================================================
-- V6 : Relax paathshaala coordinate constraints
-- ============================================================
-- Allow lat/lng to be NULL so a paathshaala can be created even
-- when the map link can't be parsed (confidence = 'unresolved').
-- Add 'manual' and 'unresolved' to the confidence CHECK.
-- ============================================================

ALTER TABLE paathshaalas ALTER COLUMN latitude DROP NOT NULL;
ALTER TABLE paathshaalas ALTER COLUMN longitude DROP NOT NULL;

ALTER TABLE paathshaalas DROP CONSTRAINT IF EXISTS paathshaalas_coordinate_confidence_check;
ALTER TABLE paathshaalas ADD CONSTRAINT paathshaalas_coordinate_confidence_check
    CHECK (coordinate_confidence IN ('parsed', 'fallback', 'manual', 'unresolved'));
