package io.github.darlene.leakdetectionapplication.analytics;

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

import java.time.LocalDateTime;

/**
 * Response DTO representing system latency statistics
 * Returned via GET /api/analytics/latency/stats
 * Used to populate the Simulation page with average, min, max latency and total request counts.
 */

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class LatencyStatsResponse {

    private double averageLatency;

    private double minLatency;

    private double maxLatency;

    private int totalRequests;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMeasuredAt;

}