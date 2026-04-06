package io.github.darlene.leakdetectionapplication.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import io.github.darlene.leakdetectionapplication.dto.response.FaultAlertResponse;
import io.github.darlene.leakdetectionapplication.service.AlertService;
import io.github.darlene.leakdetectionapplication.domain.FaultClass;

import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@Slf4j
@Validated
@RequiredArgsConstructor
@Tag(name = "Alerts")
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/recent")
    public ResponseEntity<Page<FaultAlertResponse>> getRecentAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching recent alerts - page: {}, size: {}", page, size);
        Page<FaultAlertResponse> alerts = alertService.getRecentAlerts(page, size);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/history")
    public ResponseEntity<List<FaultAlertResponse>> getAlertsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        log.info("Fetching alerts from {} to {}", from, to);
        List<FaultAlertResponse> alerts = alertService.getAlertsByDateRange(from, to);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FaultAlertResponse> getAlertById(@PathVariable Long id) {
        log.info("Fetching alert by ID: {}", id);
        FaultAlertResponse alert = alertService.getAlertById(id);
        return ResponseEntity.ok(alert);
    }

    @GetMapping("/fault-class/{faultClass}")
    public ResponseEntity<List<FaultAlertResponse>> getAlertsByFaultClass(@PathVariable FaultClass faultClass) {
        log.info("Fetching alerts by fault class: {}", faultClass);
        List<FaultAlertResponse> alerts = alertService.getAlertsByFaultClass(faultClass);
        return ResponseEntity.ok(alerts);
    }
}