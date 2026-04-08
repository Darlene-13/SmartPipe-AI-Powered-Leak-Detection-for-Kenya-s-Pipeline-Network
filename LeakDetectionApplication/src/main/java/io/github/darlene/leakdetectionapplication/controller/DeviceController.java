package io.github.darlene.leakdetectionapplication.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;

import io.github.darlene.leakdetectionapplication.service.SensorReadingService;
import io.github.darlene.leakdetectionapplication.repository.SensorReadingRepository;
import io.github.darlene.leakdetectionapplication.dto.response.SensorReadingResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for ESP32 device status and diagnostics.
 * Derives device connectivity from recency of sensor readings.
 * Accessible by OPERATOR role only.
 */
@Slf4j
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "Device Management")
public class DeviceController {

    private final SensorReadingService sensorReadingService;
    private final SensorReadingRepository sensorReadingRepository;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAllDeviceStatuses() {
        log.info("Fetching device status");

        String status;
        LocalDateTime lastSeen = null;

        var page = sensorReadingRepository
                .findAllByOrderByTimestampDesc(PageRequest.of(0, 1));

        if (!page.isEmpty()) {
            SensorReadingResponse latest = sensorReadingService
                    .getReadingById(page.getContent().get(0).getId());
            lastSeen = latest.getTimestamp();
            status = lastSeen.isAfter(LocalDateTime.now().minusSeconds(60))
                    ? "ONLINE" : "OFFLINE";
        } else {
            status = "OFFLINE";
        }

        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("deviceId", "ESP32_NODE_01");
        statusMap.put("status", status);
        statusMap.put("lastSeen", lastSeen);
        statusMap.put("totalReadings",
                sensorReadingService.getTotalReadingCount());

        return ResponseEntity.ok(statusMap);
    }

    @GetMapping("/{deviceId}/diagnostics")
    public ResponseEntity<Map<String, Object>> getDeviceDiagnostics(
            @PathVariable String deviceId) {
        log.info("Fetching diagnostics for device: {}", deviceId);

        List<SensorReadingResponse> readings =
                sensorReadingService.getReadingsByDeviceId(deviceId);

        SensorReadingResponse latestReading = readings.isEmpty()
                ? null : readings.get(0);

        Map<String, Object> diagnosticsMap = new HashMap<>();
        diagnosticsMap.put("deviceId", deviceId);
        diagnosticsMap.put("totalReadings", readings.size());
        diagnosticsMap.put("latestReading", latestReading);
        diagnosticsMap.put("latestTimestamp",
                latestReading != null ? latestReading.getTimestamp() : null);

        return ResponseEntity.ok(diagnosticsMap);
    }
}