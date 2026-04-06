package io.github.darlene.leakdetectionapplication.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;


import jakarta.validation.Valid;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.darlene.leakdetectionapplication.service.AlertService;
import io.github.darlene.leakdetectionapplication.service.LatencyTrackingService;
import io.github.darlene.leakdetectionapplication.service.MLBridge;
import io.github.darlene.leakdetectionapplication.service.CacheService;

import io.github.darlene.leakdetectionapplication.dto.Response.SystemStatusResponse;
import io.github.darlene.leakdetectionapplication.dto.Response.LatencyStatsResponse;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/status")
@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "System status")    // OpenAI/ Swagger documentation
public class StatusController{

    private final AlertService alertService;
    private final LatencyTrackingService latencyTrackingService;
    private final MLBridge mlBridge;
    private final CacheService cacheService;

    @GetMapping("/current")
    public ResponseEntity<SystemStatusResponse> getCurrentStatus(){
        log.info("Fetching current system status");

        Optional<FaultAlertResponse> mostRecentAlert = alertService.getRecentAlert();

        SystemStatus systemStatus;
        String colorCode;
        boolean requiresAction;

        if (mostRecentAlert.isPresent()){
            FaultAlertResponse alert = mostRecentAlert.get();
            systemStatus = mapFaultClassToSystemStatus(alert.getFaultClass());
            colorCode = systemStatus.getColorCode();
            requiresAction = systemStatus.isRequiresAction();

        } else {
            systemStatus = SystemStatus.NORMAL;
            colorCode = "#00FF00";
            requiresAction = false;
        }

        long activeAlerts = alertService.countRecentAlerts();

        SystemStatusResponse response = SystemStatusResponse.builder()
                .status(systemStatus.name())
                .description(systemStatus.getDescription())
                .colorCode(colorCode)
                .requiresAction(requiresAction)
                .lastUpdatedAction(LocalDatetTime.now())
                .activeAlerts(activeAlerts)
                .build();

        return ResponseEntity.ok(response);

    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth(){
        log.info("Fetching system health status");
        boolean mlBridgeServiceHealthy = mlBridgeService.isMLServiceHealthy();
        boolean cacheServiceHealthy = cacheService.isRedisHealthy();
        booleak databaseHealthy = true;

        String overallStatus = (mlBridgeServiceHealthy && cacheServiceHealthy) ? "UP" : "DEGRADED";

        Map<String, Object> healthMap = new HashMap<>();
        healthMap.put("mlService", mlServiceHealthy);
        healthMap.put(("redis", redisServiceHealthy));
        healthMap.put("database", databaseHealthy);
        healthMap.put("status", overallStatus);

        return ResponseEntity.ok(healthMap);
    }

    @GetMapping("/latency")
    public ResponseEntity<LatencyStatsResponse> getLatestLatency(){
        log.info("Fetching latest latency");

        LatencyStatsResponse latencyStats = LatencyTrackingService.getLatencyStatsResponse();
        return ResponseEntity.ok(latencyStats);
    }
}