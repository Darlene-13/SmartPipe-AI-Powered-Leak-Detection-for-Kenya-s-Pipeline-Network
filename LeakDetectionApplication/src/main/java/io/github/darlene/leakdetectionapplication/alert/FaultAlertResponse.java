package io.github.darlene.leakdetectionapplication.alert;

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
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.OffsetDateTime;

/**
 * Represents a detected fault event (AI classified and with recommendations).
 * Returned from:  GET /api/alerts/recent
 *                 GET /api/alerts/history
 *                 GET /api/alerts/{id}
 * Also broadcast via WebSocket to all connected dashboard clients
 * the moment a fault is detected.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FaultAlertResponse {

    private Long   id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime readingTime;

    private String faultClass;
    private String faultDescription;
    private String severityLevel;
    private String severityDescription;
    private Double confidence;
    private String recommendation;
    private Long   latencyMs;
    private String colorCode;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime createdAt;
}