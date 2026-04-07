package io.github.darlene.leakdetectionapplication.controller;


import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.http.ResponseEntity;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.tags.Tag;


import io.github.darlene.leakdetectionapplication.service.ProcessingService;
import io.github.darlene.leakdetectionapplication.service.CacheService;

import io.github.darlene.leakdetectionapplication.dto.request.SimulationRequest;
import io.github.darlene.leakdetectionapplication.dto.response.FaultALertResponse;

import java.util.List;


@RestController
@RequestMapping("/api/simulate")
@RequiredArgsConstructor
@slf4j
@Tag(name = "Simulation")

public class SimulationController{

    private final ProcessingService processingService;
    private final CacheService cacheService;

    @GetMapping("/scenarios")
    public ResponseEntity<List<>> getAvailableScenarios(){
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

}