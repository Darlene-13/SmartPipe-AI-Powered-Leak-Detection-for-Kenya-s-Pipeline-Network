package io.github.darlene.leakdetection.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO representing aggregated leak detection analytics
 * Returned by GET /api/analytics/summary
 * Used to populate History page charts with counts, averages, min/max, and total volume.
 */

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AnalyticsSummaryResponse {

    private int normalCount;

    private int leakCount;

    private int blockageCount;

    private int totalReadings;

    private double averagePressure;

    private double minPressure;

    private double maxPressure;

    private double averageFlowVelocity;

    private double totalVolume;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fromDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime toDate;

}