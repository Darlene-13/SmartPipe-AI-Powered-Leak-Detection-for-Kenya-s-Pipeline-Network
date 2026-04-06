package io.github.darlene.leakdetectionapplication.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.darlene.leakdetectionapplication.service.SensorReadingService;
import io.github.darlene.leakdetectionapplication.dto.response.SensorReadingResponse;

import java.util.List;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
@Tag(name = "Sensor Reading")
@Slf4j
public class SensorController {

    private final SensorReadingService sensorReadingService;

    @GetMapping("/readings/latest")
    public ResponseEntity<Page<SensorReadingResponse>> getLatestReadings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<SensorReadingResponse> readings = sensorReadingService.getLatestReadings(page, size);
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/readings/history")
    public ResponseEntity<List<SensorReadingResponse>> getReadingsByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<SensorReadingResponse> readings = sensorReadingService.getReadingsByDateRange(from, to);
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/readings/{id}")
    public ResponseEntity<SensorReadingResponse> getReadingById(@PathVariable Long id) {
        SensorReadingResponse reading = sensorReadingService.getReadingById(id);
        return ResponseEntity.ok(reading);
    }

    @GetMapping("/readings/device/{deviceId}")
    public ResponseEntity<List<SensorReadingResponse>> getReadingsByDevice(@PathVariable String deviceId) {
        List<SensorReadingResponse> readings = sensorReadingService.getReadingsByDeviceId(deviceId);
        return ResponseEntity.ok(readings);
    }
}