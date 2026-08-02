package io.github.darlene.leakdetectionapplication.monitoring;

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

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;

import io.github.darlene.leakdetectionapplication.alert.AlertService;
import io.github.darlene.leakdetectionapplication.analytics.LatencyTrackingService;
import io.github.darlene.leakdetectionapplication.pipeline.MLBridgeService;
import io.github.darlene.leakdetectionapplication.pipeline.CacheService;
import io.github.darlene.leakdetectionapplication.monitoring.SystemStatusResponse;
import io.github.darlene.leakdetectionapplication.alert.FaultAlertResponse;
import io.github.darlene.leakdetectionapplication.analytics.LatencyStatsResponse;
import io.github.darlene.leakdetectionapplication.monitoring.SystemStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller exposing current pipeline system status.
 * Derives system status from most recent fault alert.
 * Also exposes ML service and Redis health checks.
 * Accessible by OPERATOR and VIEWER roles.
 */
@Slf4j
@RestController
@RequestMapping("/api/status")
@RequiredArgsConstructor
@Tag(name = "System Status")
public class StatusController {

    private final AlertService           alertService;
    private final LatencyTrackingService latencyTrackingService;
    private final MLBridgeService        mlBridgeService;
    private final CacheService           cacheService;

    @GetMapping("/current")
    public ResponseEntity<SystemStatusResponse> getCurrentStatus() {
        log.info("Fetching current system status");

        Optional<FaultAlertResponse> mostRecentAlert =
                alertService.getMostRecentAlert();

        SystemStatus systemStatus;
        String colorCode;
        boolean requiresAction;

        // Only treat an alert as "current" if it was created within the last 2 minutes.
        // Without this window, a stale LEAK alert from a previous run permanently locks
        // the status banner — the REST poll will keep returning LEAK forever.
        OffsetDateTime twoMinutesAgo = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(2);

        if (mostRecentAlert.isPresent() &&
                mostRecentAlert.get().getCreatedAt() != null &&
                mostRecentAlert.get().getCreatedAt().isAfter(twoMinutesAgo)) {
            FaultAlertResponse alert = mostRecentAlert.get();
            systemStatus   = mapFaultClassToSystemStatus(alert.getFaultClass());
            colorCode      = systemStatus.getColorCode();
            requiresAction = systemStatus.isRequiresAction();
        } else {
            systemStatus   = SystemStatus.NORMAL;
            colorCode      = "#00FF00";
            requiresAction = false;
        }

        long activeAlerts = alertService.getRecentAlerts(0, 10)
                .getTotalElements();

        SystemStatusResponse response = SystemStatusResponse.builder()
                .status(systemStatus.name())
                .description(systemStatus.getDescription())
                .colorCode(colorCode)
                .requiresAction(requiresAction)
                .lastUpdated(OffsetDateTime.now(ZoneOffset.UTC))
                .activeAlerts((int) activeAlerts)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        log.info("Fetching system health status");

        boolean mlServiceHealthy = mlBridgeService.isMLServiceHealthy();
        boolean redisHealthy     = cacheService.isRedisHealthy();

        String overallStatus = (mlServiceHealthy && redisHealthy) ? "UP" : "DEGRADED";

        Map<String, Object> healthMap = new HashMap<>();
        healthMap.put("mlService", mlServiceHealthy);
        healthMap.put("redis",     redisHealthy);
        healthMap.put("database",  true);
        healthMap.put("status",    overallStatus);

        return ResponseEntity.ok(healthMap);
    }

    @GetMapping("/latency")
    public ResponseEntity<LatencyStatsResponse> getLatestLatency() {
        log.info("Fetching latest latency stats");
        return ResponseEntity.ok(latencyTrackingService.getLatencyStatsResponse());
    }

    private SystemStatus mapFaultClassToSystemStatus(String faultClass) {
        return switch (faultClass) {
            case "LEAK"     -> SystemStatus.LEAK_DETECTED;
            case "BLOCKAGE" -> SystemStatus.BLOCKAGE_DETECTED;
            default         -> SystemStatus.NORMAL;
        };
    }
}