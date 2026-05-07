package io.github.darlene.leakdetectionapplication.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a single sensor reading from the ESP32 node.
 * Matches the actual ESP32 payload and live_feature_dataset.csv
 *  reading_time column name kept consistent with DB schema
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sensor_readings")
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @NotNull
    @Column(name = "reading_time", nullable = false)
    private LocalDateTime readingTime;

    @NotNull @Positive
    @Column(name = "node_a_pressure", nullable = false)
    private Double nodeAPressure;

    @NotNull @Positive
    @Column(name = "velocity_a", nullable = false)
    private Double velocityA;

    @NotNull @Positive
    @Column(name = "node_b_pressure", nullable = false)
    private Double nodeBPressure;

    @NotNull @Positive
    @Column(name = "velocity_b", nullable = false)
    private Double velocityB;

    @NotNull @Positive
    @Column(name = "node_c_pressure", nullable = false)
    private Double nodeCPressure;

    @NotNull @Positive
    @Column(name = "velocity_c", nullable = false)
    private Double velocityC;

    @Column(name = "dp_dt_a")
    private Double dpDtA;

    @Column(name = "dp_dt_b")
    private Double dpDtB;

    @Column(name = "dp_dt_c")
    private Double dpDtC;

    @Column(name = "scenario")
    private String scenario;

    @Column(name = "prediction")
    private String prediction;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}