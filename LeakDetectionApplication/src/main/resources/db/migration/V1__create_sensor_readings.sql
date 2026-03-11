CREATE TABLE sensor_readings (
                                 id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 device_id         VARCHAR(255) NOT NULL,
                                 timestamp         TIMESTAMP NOT NULL,
                                 node_a_pressure   DOUBLE PRECISION NOT NULL,
                                 node_b_pressure   DOUBLE PRECISION NOT NULL,
                                 node_c_pressure   DOUBLE PRECISION NOT NULL,
                                 flow_velocity     DOUBLE PRECISION NOT NULL,
                                 dp_dt             DOUBLE PRECISION,
                                 scenario          VARCHAR(255),
                                 created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);