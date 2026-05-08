package io.github.darlene.leakdetectionapplication.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for aggregated analytics.
 * Returned by GET /api/analytics/summary
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryResponse {
    private int    normalCount;
    private int    leakCount;
    private int    blockageCount;
    private int    totalReadings;
    private double averagePressure;
    private double minPressure;
    private double maxPressure;
    private double averageVelocity;
    private double totalVolume;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fromDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime toDate;
}