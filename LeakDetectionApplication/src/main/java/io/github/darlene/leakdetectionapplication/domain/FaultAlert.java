package io.github.darlene.leakdetectionapplication.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a detected fault event in the pipeline system.
 * Created when the ML classifier detects a LEAK or BLOCKAGE.
 * Each alert links to the SensorReading that triggered it
 * and contains the AI generated operator recommendation.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fault_alerts")


public class FaultAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn( name = "sensor_reading_id", nullable = false)
    private SensorReading sensorReading;


    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "fault_class", nullable = false)
    private FaultClass faultClass;


    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "severity_level", nullable = false)
    private SeverityLevel severityLevel;

    @Column(name = "confidence",precision = 5, scale = 4 , nullable = false)
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double confidence;

    @Column(name = "recommendation", columnDefinition = "TEXT")
    private String recommendation;

    @NotNull
    @Positive
    @Column(name = "latency_ms")
    private Long latencyMs;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}