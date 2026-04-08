package io.github.darlene.leakdetectionapplication.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import io.github.darlene.leakdetectionapplication.service.AnalyticsService;
import io.github.darlene.leakdetectionapplication.dto.response.AnalyticsSummaryResponse;
import io.github.darlene.leakdetectionapplication.dto.response.LatencyStatsResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST controller exposing pipeline analytics data.
 * Provides summary statistics, latency stats and fault distribution.
 * Accessible by OPERATOR and VIEWER roles.
 */
@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryResponse> getAnalyticsSummary(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        log.info("Fetching analytics summary from {} to {}", from, to);
        AnalyticsSummaryResponse summary =
                analyticsService.getSummary(from, to);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/latency/stats")
    public ResponseEntity<LatencyStatsResponse> getLatencyStats() {
        log.info("Fetching latency statistics");
        LatencyStatsResponse stats = analyticsService.getLatencyStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/fault-distribution")
    public ResponseEntity<Map<String, Long>> getFaultDistribution(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        log.info("Fetching fault distribution from {} to {}", from, to);
        Map<String, Long> distribution =
                analyticsService.getFaultDistribution(from, to);
        return ResponseEntity.ok(distribution);
    }
}