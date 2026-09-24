-- ============================================================
-- V14 : Add supervisor timetable and overrides
-- ============================================================

CREATE TABLE supervisor_timetable (
    id UUID PRIMARY KEY,
    supervisor_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    day_of_week INTEGER NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    slot VARCHAR(10) NOT NULL CHECK (slot IN ('MORNING', 'EVENING')),
    paathshaala_id UUID REFERENCES paathshaalas(id) ON DELETE SET NULL,
    UNIQUE (supervisor_id, day_of_week, slot)
);

CREATE TABLE supervisor_timetable_overrides (
    id UUID PRIMARY KEY,
    supervisor_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    override_date DATE NOT NULL,
    slot VARCHAR(10) NOT NULL CHECK (slot IN ('MORNING', 'EVENING')),
    paathshaala_id UUID REFERENCES paathshaalas(id) ON DELETE SET NULL,
    UNIQUE (supervisor_id, override_date, slot)
);
