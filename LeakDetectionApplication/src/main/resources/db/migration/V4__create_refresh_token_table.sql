-- ==================================================
-- Description: Refresh token table for JWT/session management
-- ==================================================

CREATE TABLE IF NOT EXISTS refresh_token
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token       VARCHAR(512) NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    expiry_date TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

-- Fast lookup of all tokens belonging to a user
CREATE INDEX IF NOT EXISTS idx_refresh_token_token
    ON refresh_token (token);

CREATE INDEX IF NOT EXISTS idx_refresh_token_user
    ON refresh_token (user_id);