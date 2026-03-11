CREATE TABLE fault_alerts (
                              id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              sensor_reading_id BIGINT NOT NULL,
                              fault_class       VARCHAR(50) NOT NULL,
                              severity_level    VARCHAR(50) NOT NULL,
                              confidence        DOUBLE PRECISION NOT NULL,
                              recommendation    TEXT,
                              latency_ms        BIGINT NOT NULL,
                              created_at        TIMESTAMP NOT NULL DEFAULT NOW(),

                              CONSTRAINT fk_sensor_reading
                                  FOREIGN KEY (sensor_reading_id)
                                      REFERENCES sensor_readings(id),

                              CONSTRAINT chk_confidence
                                  CHECK (confidence >= 0.0 AND confidence <= 1.0)
);