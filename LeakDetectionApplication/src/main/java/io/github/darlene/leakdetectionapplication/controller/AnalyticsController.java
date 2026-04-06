package io.github.darlene.leakdetectionapplication.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import io.github.darlene.leakdetectionapplication.service.AlertService;
import io.github.darlene.leakdetectionapplication.service.LatencyTrackingService;
import io.github.darlene.leakdetectionapplication.service.SensorReadingService;

import io.github.darlene.leakdetectionapplication.dto.response.AnalyticsServiceResponse;
import io.github.darlene.leakdetectionapplication.dto.response.LatencyStatsResponse;

import java.util.LocalDateTime;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Analytics")
@RequestMapping("/api/analytics")



public class AnalyticsController{

    private final AlertService alertService;
    private final LatencyTrackingService latencyTrackingService;
    private final SensorReadingService sensorReadingService;

    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummaryResponse> getAnalyticsSummary(@RequestParam @DateTimeFormat from,
                                              @RequestParam @DateTimeFormat to){
        log.info("Fetching analytics summary from {} to {}");
        AlertService alertService = alertService.getAnalyticsSummary(from, to);
        return ResponseEntity.ok(summary)
    }

}