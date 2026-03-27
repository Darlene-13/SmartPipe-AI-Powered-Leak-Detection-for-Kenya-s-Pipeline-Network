-- ==================================================
-- Description: Fault alerts table linked to sensor_readings
-- ==================================================

CREATE TABLE fault_alerts (
                              id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              sensor_reading_id     BIGINT           NOT NULL,
                              sensor_reading_time   TIMESTAMPTZ      NOT NULL,
                              fault_class           VARCHAR(50)      NOT NULL,
                              severity_level        VARCHAR(50)      NOT NULL,
                              confidence            DOUBLE PRECISION NOT NULL,
                              latency_ms            BIGINT           NOT NULL,
                              created_at            TIMESTAMPTZ      NOT NULL DEFAULT NOW(),

    -- Foreign key references the composite PK of sensor_readings
                              CONSTRAINT fk_sensor_reading
                                  FOREIGN KEY (sensor_reading_id, sensor_reading_time)
                                      REFERENCES sensor_readings(id, reading_time),

    -- Check constraints
                              CONSTRAINT chk_confidence
                                  CHECK (confidence >= 0.0 AND confidence <= 1.0),

                              CONSTRAINT chk_severity
                                  CHECK (severity_level IN ('LOW','MEDIUM','HIGH','CRITICAL'))
);

-- Indexes for fast queries

-- For recent alerts queries
CREATE INDEX idx_fault_alerts_created
    ON fault_alerts (created_at DESC);

-- For severity-based dashboards
CREATE INDEX idx_fault_alerts_severity
    ON fault_alerts (severity_level, created_at DESC);

-- For joining with sensor readings
CREATE INDEX idx_fault_alerts_sensor
    ON fault_alerts (sensor_reading_id, sensor_reading_time);