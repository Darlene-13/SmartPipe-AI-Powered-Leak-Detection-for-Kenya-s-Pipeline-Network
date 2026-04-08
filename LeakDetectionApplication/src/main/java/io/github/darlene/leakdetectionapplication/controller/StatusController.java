package io.github.darlene.leakdetectionapplication.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;

import io.github.darlene.leakdetectionapplication.service.AlertService;
import io.github.darlene.leakdetectionapplication.service.LatencyTrackingService;
import io.github.darlene.leakdetectionapplication.service.MLBridgeService;
import io.github.darlene.leakdetectionapplication.service.CacheService;
import io.github.darlene.leakdetectionapplication.dto.response.SystemStatusResponse;
import io.github.darlene.leakdetectionapplication.dto.response.FaultAlertResponse;
import io.github.darlene.leakdetectionapplication.dto.response.LatencyStatsResponse;
import io.github.darlene.leakdetectionapplication.domain.SystemStatus;
import io.github.darlene.leakdetectionapplication.domain.FaultClass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
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

    private final AlertService alertService;
    private final LatencyTrackingService latencyTrackingService;
    private final MLBridgeService mlBridgeService;
    private final CacheService cacheService;

    @GetMapping("/current")
    public ResponseEntity<SystemStatusResponse> getCurrentStatus() {
        log.info("Fetching current system status");

        Optional<FaultAlertResponse> mostRecentAlert =
                alertService.getMostRecentAlert();

        SystemStatus systemStatus;
        String colorCode;
        boolean requiresAction;

        if (mostRecentAlert.isPresent()) {
            FaultAlertResponse alert = mostRecentAlert.get();
            systemStatus = mapFaultClassToSystemStatus(alert.getFaultClass());
            colorCode = systemStatus.getColorCode();
            requiresAction = systemStatus.isRequiresAction();
        } else {
            systemStatus = SystemStatus.NORMAL;
            colorCode = "#00FF00";
            requiresAction = false;
        }

        long activeAlerts = alertService.getRecentAlerts(0, 10)
                .getTotalElements();

        SystemStatusResponse response = SystemStatusResponse.builder()
                .status(systemStatus.name())
                .description(systemStatus.getDescription())
                .colorCode(colorCode)
                .requiresAction(requiresAction)
                .lastUpdated(LocalDateTime.now())
                .activeAlerts((int) activeAlerts)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        log.info("Fetching system health status");

        boolean mlServiceHealthy = mlBridgeService.isMLServiceHealthy();
        boolean redisHealthy = cacheService.isRedisHealthy();
        boolean databaseHealthy = true;

        String overallStatus = (mlServiceHealthy && redisHealthy)
                ? "UP" : "DEGRADED";

        Map<String, Object> healthMap = new HashMap<>();
        healthMap.put("mlService", mlServiceHealthy);
        healthMap.put("redis", redisHealthy);
        healthMap.put("database", databaseHealthy);
        healthMap.put("status", overallStatus);

        return ResponseEntity.ok(healthMap);
    }

    @GetMapping("/latency")
    public ResponseEntity<LatencyStatsResponse> getLatestLatency() {
        log.info("Fetching latest latency stats");
        LatencyStatsResponse latencyStats =
                latencyTrackingService.getLatencyStatsResponse();
        return ResponseEntity.ok(latencyStats);
    }

    private SystemStatus mapFaultClassToSystemStatus(String faultClass) {
        return switch (faultClass) {
            case "LEAK"     -> SystemStatus.LEAK_DETECTED;
            case "BLOCKAGE" -> SystemStatus.BLOCKAGE_DETECTED;
            default         -> SystemStatus.NORMAL;
        };
    }
}