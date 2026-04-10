-- ==================================================
-- Description: Users table for authentication and roles
-- ==================================================
CREATE TABLE users
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    username   VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'ROLE_OPERATOR',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    -- Ensure role matches Java UserRole enum values
    CONSTRAINT chk_role
        CHECK (role IN ('ROLE_OPERATOR', 'ROLE_VIEWER'))
);
-- Index for fast login lookup
CREATE INDEX idx_users_username
    ON users (username);
-- Index for role-based queries
CREATE INDEX idx_users_role
    ON users (role);