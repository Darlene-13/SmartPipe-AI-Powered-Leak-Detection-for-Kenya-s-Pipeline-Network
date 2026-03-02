package io.github.darlene.leakdetectionapplication.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a single sensor reading received from the ESP32 node.
 * Each instance maps to one row in the sensor_readings table.
 * Contains pressure readings from all three nodes, flow velocity,
 * and the calculated pressure gradient (dP/dt).
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
    @Column(name = "timestamp", nullable = false )
    private LocalDateTime timestamp;

    @NotNull
    @Positive
    @Column(name = "node_a_pressure", nullable = false)
    private Double nodeAPressure;

    @NotNull
    @Positive
    @Column(name = "node_b_pressure", nullable = false)
    private Double nodeBPressure;

    @NotNull
    @Positive
    @Column(name = "node_c_pressure", nullable = false)
    private Double nodeCPressure;

    @NotNull
    @Positive
    @Column(name = "flow_velocity", nullable = false)
    private Double flowVelocity;

    @Column(name = "dp_dt")
    private Double dpDt;

    @Column(name = "scenario")
    private String scenario;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}