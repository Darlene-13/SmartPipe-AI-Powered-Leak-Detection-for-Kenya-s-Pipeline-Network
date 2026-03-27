package io.github.darlene.leakdetectionapplication.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * Request DTO representing a sensor telemetry packet
 * received from the ESP32 node via MQTT.
 * Maps to topic: pipeline/sensors/node
 * Validated before processing by ProcessingService.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor

public class SensorReadingRequest {

    @JsonProperty("id")
    @NotBlank(message = "Device Id cannot be blank")
    private String deviceId;

    @JsonProperty("ts")
    @NotNull(message = "Every Reading should have a timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("pa")
    @NotNull(message = "Node A pressure reading is required")
    @Positive(message = "Node A pressure must be positive")
    @DecimalMin(value = "50000.0", message = "Pressure below operational minimum")
    @DecimalMax(value = "1000000.0", message = "Pressure exceeds operational maximum")
    private Double nodeAPressure;

    @JsonProperty("pb")
    @NotNull(message = "Node B pressure reading is required")
    @Positive(message = "Node B pressure must be positive")
    @DecimalMin(value = "50000.0", message = "Pressure below operational minimum")
    @DecimalMax(value = "1000000.0", message = "Pressure exceeds operational maximum")
    private Double nodeBPressure;

    @JsonProperty("pc")
    @NotNull(message = "Node C pressure reading is required")
    @Positive (message = "Node C pressure must be positive")
    @DecimalMin(value = "50000.0", message = "Pressure below operational minimum")
    @DecimalMax(value = "1000000.0", message = "Pressure exceeds operational maximum")
    private Double nodeCPressure;

    @JsonProperty("fv")
    @NotNull
    @DecimalMin(value = "0.5", message = "Flow velocity below minimum operational threshold")
    @DecimalMax(value = "10.0", message = "Flow velocity exceeds maximum operational threshold")
    @DecimalMin("0.5")
    @DecimalMax("10.0")
    private Double flowVelocity;

    @JsonProperty("sc")
    private String scenario;

}



