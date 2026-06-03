package io.github.darlene.leakdetectionapplication.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

/**
 * Response DTO representing a single sensor reading.
 * Returned by GET /api/sensors/readings/latest
 * and GET /api/sensors/readings/history
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SensorReadingResponse {
    private Long   id;
    private String deviceId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime readingTime;

    // Pressures
    private Double nodeAPressure;
    private Double nodeBPressure;
    private Double nodeCPressure;

    // Velocities — three nodes
    private Double velocityA;
    private Double velocityB;
    private Double velocityC;

    // Derived
    private Double dpDtA;
    private Double dpDtB;
    private Double dpDtC;
    private String scenario;
    private String prediction;
}