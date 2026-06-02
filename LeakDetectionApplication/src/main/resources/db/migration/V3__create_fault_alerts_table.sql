CREATE TABLE fault_alerts (
                              id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                              sensor_reading_id BIGINT NOT NULL,

                              device_id VARCHAR(255) NOT NULL,

                              fault_class VARCHAR(50) NOT NULL,
                              severity_level VARCHAR(50) NOT NULL,

                              confidence DOUBLE PRECISION NOT NULL,

                              prob_normal DOUBLE PRECISION,
                              prob_warning DOUBLE PRECISION,
                              prob_critical DOUBLE PRECISION,

                              latency_ms BIGINT,

                              recommendation TEXT,

                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                              CONSTRAINT fk_sensor_reading
                                  FOREIGN KEY (sensor_reading_id)
                                      REFERENCES sensor_readings (id)
                                      ON DELETE CASCADE
);

CREATE INDEX idx_fault_alerts_sensor
    ON fault_alerts (sensor_reading_id);

CREATE INDEX idx_fault_alerts_device
    ON fault_alerts (device_id);