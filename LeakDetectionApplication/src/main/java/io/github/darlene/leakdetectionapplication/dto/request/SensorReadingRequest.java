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
import lombok.Builder;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SensorReadingRequest {

    @JsonProperty("device_id")
    @NotBlank(message = "Device ID cannot be blank")
    private String deviceId;

    @JsonProperty("ts")
    private LocalDateTime readingTime;

    @JsonProperty("node_a_pressure")
    @NotNull(message = "Node A pressure is required")
    @Positive(message = "Node A pressure must be positive")
    @DecimalMin(value = "500.0",     message = "Pressure below operational minimum")
    @DecimalMax(value = "1000000.0", message = "Pressure exceeds operational maximum")
    private Double nodeAPressure;

    @JsonProperty("velocity_a")
    @NotNull(message = "Velocity A is required")
    @DecimalMin(value = "0.1", message = "Velocity A below minimum")
    @DecimalMax(value = "20.0", message = "Velocity A exceeds maximum")
    private Double velocityA;

    @JsonProperty("node_b_pressure")
    @NotNull(message = "Node B pressure is required")
    @Positive(message = "Node B pressure must be positive")
    @DecimalMin(value = "500.0",     message = "Pressure below operational minimum")
    @DecimalMax(value = "1000000.0", message = "Pressure exceeds operational maximum")
    private Double nodeBPressure;

    @JsonProperty("velocity_b")
    @NotNull(message = "Velocity B is required")
    @DecimalMin(value = "0.1", message = "Velocity B below minimum")
    @DecimalMax(value = "20.0", message = "Velocity B exceeds maximum")
    private Double velocityB;

    @JsonProperty("node_c_pressure")
    @NotNull(message = "Node C pressure is required")
    @Positive(message = "Node C pressure must be positive")
    @DecimalMin(value = "500.0",     message = "Pressure below operational minimum")
    @DecimalMax(value = "1000000.0", message = "Pressure exceeds operational maximum")
    private Double nodeCPressure;

    @JsonProperty("velocity_c")
    @NotNull(message = "Velocity C is required")
    @DecimalMin(value = "0.1", message = "Velocity C below minimum")
    @DecimalMax(value = "20.0", message = "Velocity C exceeds maximum")
    private Double velocityC;

    @JsonProperty("sc")
    private String scenario;
}