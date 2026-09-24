-- ============================================================
-- V15 : Add geofence state and events
-- ============================================================

CREATE TABLE user_geofence_state (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    state_date DATE NOT NULL,
    is_at_home BOOLEAN NOT NULL,
    is_at_school BOOLEAN NOT NULL,
    last_updated TIMESTAMP NOT NULL
);

CREATE TABLE geofence_events (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_type VARCHAR(20) NOT NULL CHECK (event_type IN ('LEFT_HOME', 'REACHED_SCHOOL', 'LEFT_SCHOOL', 'REACHED_HOME')),
    event_time TIMESTAMP NOT NULL,
    event_date DATE NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,
    paathshaala_id UUID REFERENCES paathshaalas(id) ON DELETE SET NULL
);
