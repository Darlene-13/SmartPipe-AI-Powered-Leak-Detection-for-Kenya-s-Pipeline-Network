package io.github.darlene.leakdetectionapplication.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import io.github.darlene.leakdetectionapplication.service.SensorReadingService;
import io.github.darlene.leakdetectionapplication.dto.response.SensorReadingResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for sensor reading queries.
 * Provides paginated, date-filtered and device-filtered access
 * to pipeline sensor telemetry data.
 * Accessible by OPERATOR role only.
 */
@Slf4j
@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
@Tag(name = "Sensor Readings")
public class SensorController {

    private final SensorReadingService sensorReadingService;

    @GetMapping("/readings/latest")
    public ResponseEntity<Page<SensorReadingResponse>> getLatestReadings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        log.info("Fetching latest readings - page: {}, size: {}", page, size);
        Page<SensorReadingResponse> readings =
                sensorReadingService.getLatestReadings(page, size);
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/readings/history")
    public ResponseEntity<List<SensorReadingResponse>> getReadingsByRange(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        log.info("Fetching readings from {} to {}", from, to);
        List<SensorReadingResponse> readings =
                sensorReadingService.getReadingsByDateRange(from, to);
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/readings/{id}")
    public ResponseEntity<SensorReadingResponse> getReadingById(
            @PathVariable Long id) {
        log.info("Fetching reading by ID: {}", id);
        SensorReadingResponse reading = sensorReadingService.getReadingById(id);
        return ResponseEntity.ok(reading);
    }

    @GetMapping("/readings/device/{deviceId}")
    public ResponseEntity<List<SensorReadingResponse>> getReadingsByDevice(
            @PathVariable String deviceId) {
        log.info("Fetching readings for device: {}", deviceId);
        List<SensorReadingResponse> readings =
                sensorReadingService.getReadingsByDeviceId(deviceId);
        return ResponseEntity.ok(readings);
    }
}