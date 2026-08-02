package io.github.darlene.leakdetectionapplication.sensor;

import io.github.darlene.leakdetectionapplication.alert.*;
import io.github.darlene.leakdetectionapplication.analytics.*;
import io.github.darlene.leakdetectionapplication.auth.*;
import io.github.darlene.leakdetectionapplication.configuration.*;
import io.github.darlene.leakdetectionapplication.messaging.*;
import io.github.darlene.leakdetectionapplication.monitoring.*;
import io.github.darlene.leakdetectionapplication.pipeline.*;
import io.github.darlene.leakdetectionapplication.recommendation.*;
import io.github.darlene.leakdetectionapplication.sensor.*;
import io.github.darlene.leakdetectionapplication.simulation.*;
import io.github.darlene.leakdetectionapplication.shared.*;

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