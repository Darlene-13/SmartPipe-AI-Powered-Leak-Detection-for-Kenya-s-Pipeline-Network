-- ============================================================
--  sensor_readings
--  Composite PK (id, reading_time) for TimescaleDB hypertable
-- ============================================================
CREATE TABLE sensor_readings (
                                 id               BIGINT GENERATED ALWAYS AS IDENTITY,
                                 device_id        VARCHAR(255)     NOT NULL,
                                 reading_time     TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
                                 node_a_pressure  DOUBLE PRECISION NOT NULL,
                                 velocity_a       DOUBLE PRECISION NOT NULL,
                                 node_b_pressure  DOUBLE PRECISION NOT NULL,
                                 velocity_b       DOUBLE PRECISION NOT NULL,
                                 node_c_pressure  DOUBLE PRECISION NOT NULL,
                                 velocity_c       DOUBLE PRECISION NOT NULL,
                                 dp_dt_a          DOUBLE PRECISION,
                                 dp_dt_b          DOUBLE PRECISION,
                                 dp_dt_c          DOUBLE PRECISION,
                                 scenario         VARCHAR(255),
                                 prediction       VARCHAR(50),
                                 created_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
                                 PRIMARY KEY (id, reading_time)
);

-- Convert to hypertable — partitions by reading_time automatically
SELECT create_hypertable('sensor_readings', 'reading_time', if_not_exists => TRUE);

CREATE INDEX idx_sensor_device_time
    ON sensor_readings (device_id, reading_time DESC);
 