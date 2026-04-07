package io.github.darlene.leakdetectionapplication.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import io.github.darlene.leakdetectionapplication.service.AnalyticsService;

import io.github.darlene.leakdetectionapplication.dto.response.AnalyticsSummaryResponse;
import io.github.darlene.leakdetectionapplication.dto.response.LatencyStatsResponse;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Analytics")
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryResponse> getAnalyticsSummary(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        log.info("Fetching analytics summary...");
        AnalyticsSummaryResponse analytics = analyticsService.getSummary(from, to);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/latency/stats")
    public ResponseEntity<LatencyStatsResponse> getLatencyStatsResponse() {
        log.info("Fetching latency stats...");
        LatencyStatsResponse stats = analyticsService.getLatencyStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/fault-distribution")
    public ResponseEntity<Map<String, Long>> getFaultListDistribution(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        log.info("Fetching fault distribution...");
        Map<String, Long> distribution = analyticsService.getFaultDistribution(from, to);
        return ResponseEntity.ok(distribution);
    }
}