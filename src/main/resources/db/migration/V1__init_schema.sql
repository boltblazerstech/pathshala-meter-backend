-- ============================================================
-- V1 : Real schema for pathshala-meter
-- ============================================================
-- The DB previously had a throwaway `location_points` table
-- created by Hibernate ddl-auto=update during the stub phase.
-- Drop it first; Flyway now owns everything from here.
-- ============================================================

DROP TABLE IF EXISTS location_points;

-- ────────────────────────────────────────────────────────────
-- 1. paathshaalas  (no FK dependencies — must come first)
-- ────────────────────────────────────────────────────────────
CREATE TABLE paathshaalas (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                    VARCHAR(255) NOT NULL,
    latitude                DOUBLE PRECISION NOT NULL,
    longitude               DOUBLE PRECISION NOT NULL,
    source_map_link         TEXT,
    -- 'parsed'   = coordinate extracted cleanly from the map link
    -- 'fallback' = coordinate could not be parsed; a default/approximate
    --              value was used instead and should be reviewed
    coordinate_confidence   VARCHAR(10) NOT NULL
                                CHECK (coordinate_confidence IN ('parsed', 'fallback')),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ────────────────────────────────────────────────────────────
-- 2. admin_users  (no FK dependencies)
-- ────────────────────────────────────────────────────────────
CREATE TABLE admin_users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ────────────────────────────────────────────────────────────
-- 3. users  (FK -> paathshaalas)
-- ────────────────────────────────────────────────────────────
CREATE TABLE users (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                    VARCHAR(255) NOT NULL,
    phone_number            VARCHAR(20) UNIQUE NOT NULL,
    role                    VARCHAR(20) NOT NULL
                                CHECK (role IN ('supervisor', 'teacher')),
    assigned_paathshaala_id UUID REFERENCES paathshaalas(id) ON DELETE SET NULL,
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),

    -- A teacher must be assigned to a paathshaala.
    -- A supervisor must NOT be assigned to any paathshaala.
    CONSTRAINT chk_role_paathshaala CHECK (
        (role = 'teacher'    AND assigned_paathshaala_id IS NOT NULL) OR
        (role = 'supervisor' AND assigned_paathshaala_id IS NULL)
    )
);

-- ────────────────────────────────────────────────────────────
-- 4. tracking_windows  (FK -> users)
-- ────────────────────────────────────────────────────────────
CREATE TABLE tracking_windows (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_time           TIME NOT NULL,
    end_time             TIME NOT NULL,
    interval_minutes     INT NOT NULL CHECK (interval_minutes > 0),
    effective_from_date  DATE NOT NULL,
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ────────────────────────────────────────────────────────────
-- 5. location_pings  (FK -> users)
--
--  NOTE: No distance / within-range column is stored here.
--  Distance from the assigned paathshaala is always computed
--  at query time using the Haversine formula so that:
--    a) historical pings are automatically re-evaluated if a
--       paathshaala's coordinates are corrected later, and
--    b) there is a single source of truth for coordinates.
-- ────────────────────────────────────────────────────────────
CREATE TABLE location_pings (
    id           BIGSERIAL PRIMARY KEY,
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lat          DOUBLE PRECISION NOT NULL,
    lng          DOUBLE PRECISION NOT NULL,
    captured_at  TIMESTAMPTZ NOT NULL,
    received_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    sync_status  VARCHAR(20) NOT NULL DEFAULT 'synced'
);

-- ────────────────────────────────────────────────────────────
-- 6. system_config  (key-value store for app-wide settings)
-- ────────────────────────────────────────────────────────────
CREATE TABLE system_config (
    key     VARCHAR(100) PRIMARY KEY,
    value   TEXT
);

-- Seed the shared password used by the Flutter field app to authenticate.
-- Value is a bcrypt hash of the placeholder password 'ChangeMe@123'.
-- Replace this hash before going to production via a new migration.
INSERT INTO system_config (key, value)
VALUES (
    'field_app_shared_password',
    '$2a$12$7QJ8ZvMkLpXtN9WqYzR3eO4KvHgJmS6DcTxUbPw1FnEaIjCylQodW'
);

-- ────────────────────────────────────────────────────────────
-- Indexes
-- ────────────────────────────────────────────────────────────
CREATE INDEX idx_users_phone_number
    ON users(phone_number);

CREATE INDEX idx_users_assigned_paathshaala_id
    ON users(assigned_paathshaala_id);

CREATE INDEX idx_tracking_windows_user_date
    ON tracking_windows(user_id, effective_from_date);

CREATE INDEX idx_location_pings_user_captured
    ON location_pings(user_id, captured_at);
