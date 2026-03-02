package io.github.darlene.leakdetection.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Represents a detected fault event(AI classified and with recommendations)
 *  It is returned from the following end points: GET /api/alerts/recent
 *                                                GET /api/alerts/history
 *                                                GET api/alerts/{id/}
 * It is also used in broadcasting via the websocket to all connected dashboard clients the moment a fault is detected.
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class FaultAlertResponse {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String faultClass;

    private String faultDescription;

    private String severityLevel;

    private String severityDescription;

    private Double confidence;

    private String recommendation;

    private Long latencyMs;

    private String colorCode;

    @JsonFormat(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}