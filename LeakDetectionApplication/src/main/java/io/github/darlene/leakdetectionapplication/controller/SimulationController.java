package io.github.darlene.leakdetectionapplication.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.darlene.leakdetectionapplication.service.ProcessingService;
import io.github.darlene.leakdetectionapplication.service.CacheService;

import io.github.darlene.leakdetectionapplication.dto.request.SimulationRequest;
import io.github.darlene.leakdetectionapplication.dto.response.FaultAlertResponse;

import java.util.List;

@RestController
@RequestMapping("/api/simulate")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Simulation")
public class SimulationController {

    private final ProcessingService processingService;
    private final CacheService cacheService;

    @GetMapping("/scenarios")
    public ResponseEntity<List<String>> getAvailableScenarios() {
        log.info("Fetching available scenarios");
        return ResponseEntity.ok(List.of(
                "NORMAL_BASELINE",
                "LEAK_INCIPIENT",
                "LEAK_MODERATE",
                "LEAK_CRITICAL",
                "BLOCKAGE_25",
                "BLOCKAGE_50",
                "BLOCKAGE_75"
        ));
    }

    @PostMapping("/scenario/{scenarioName}")
    public ResponseEntity<FaultAlertResponse> simulateScenario(@PathVariable String scenarioName) {
        log.info("Simulating scenario: {}", scenarioName);
        cacheService.clearAllPredictions();
        FaultAlertResponse alertResponse = processingService.simulateScenario(scenarioName);
        return ResponseEntity.ok(alertResponse);
    }

    @PostMapping("/inject-fault")
    public ResponseEntity<FaultAlertResponse> injectFault(@Valid @RequestBody SimulationRequest request) {
        log.info("Injecting fault: {} severity: {}", request.getFaultType(), request.getSeverity());
        cacheService.clearAllPredictions();
        FaultAlertResponse alertResponse = processingService.injectFault(request);
        return ResponseEntity.ok(alertResponse);
    }
}