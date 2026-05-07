CREATE TABLE fault_alerts (
                              id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              sensor_reading_id    BIGINT           NOT NULL,
                              sensor_reading_time  TIMESTAMPTZ      NOT NULL,
                              device_id            VARCHAR(255)     NOT NULL,
                              fault_class          VARCHAR(50)      NOT NULL,
                              severity_level       VARCHAR(50)      NOT NULL,
                              confidence           DOUBLE PRECISION NOT NULL,
                              prob_normal          DOUBLE PRECISION,
                              prob_warning         DOUBLE PRECISION,
                              prob_critical        DOUBLE PRECISION,
                              latency_ms           BIGINT,
                              recommendation       TEXT,
                              created_at           TIMESTAMPTZ      NOT NULL DEFAULT NOW(),

                              CONSTRAINT fk_sensor_reading
                                  FOREIGN KEY (sensor_reading_id, sensor_reading_time)
                                      REFERENCES sensor_readings (id, reading_time)
                                      ON DELETE CASCADE,

                              CONSTRAINT chk_confidence
                                  CHECK (confidence >= 0.0 AND confidence <= 1.0),

                              CONSTRAINT chk_severity
                                  CHECK (severity_level IN ('NONE', 'LOW', 'MODERATE', 'CRITICAL'))
);

CREATE INDEX idx_fault_alerts_created
    ON fault_alerts (created_at DESC);

CREATE INDEX idx_fault_alerts_severity
    ON fault_alerts (severity_level, created_at DESC);

CREATE INDEX idx_fault_alerts_device
    ON fault_alerts (device_id, created_at DESC);

CREATE INDEX idx_fault_alerts_sensor
    ON fault_alerts (sensor_reading_id, sensor_reading_time);

-- Verify
SELECT table_name, column_name, data_type
FROM information_schema.columns
WHERE table_name IN ('sensor_readings', 'fault_alerts')
ORDER BY table_name, ordinal_position;
 