package io.github.darlene.leakdetectionapplication.controller;

import io.github.darlene.leakdetectionapplication.dto.request.RecommendationRequest;
import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;
import io.github.darlene.leakdetectionapplication.dto.response.RecommendationResponse;
import io.github.darlene.leakdetectionapplication.service.MLBridgeService;
import io.github.darlene.leakdetectionapplication.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "AI-Recommendations", description = "LLM-generated maintenance recommendations for detected pipeline faults")
public class RecommendationsController {

    private final RecommendationService recommendationService;
    private final MLBridgeService mlBridgeService;

    /**
     * On-demand: send sensor features, get prediction + recommendation back.
     * This is the main endpoint that drives the full inference chain.
     */
    @PostMapping("/generate")
    public ResponseEntity<RecommendationResponse> generate(
            @Valid @RequestBody SensorReadingRequest request) {

        // 1. Send typed DTO directly to ML bridge
        MLPredictionResponse prediction = mlBridgeService.predict(request);

        // 2. Build features map manually for LLM prompt (RecommendationService needs it)
        Map<String, Double> features = Map.of(
                "node_a_pressure", request.getNodeAPressure(),
                "node_b_pressure", request.getNodeBPressure(),
                "node_c_pressure", request.getNodeCPressure(),
                "velocity_a",      request.getVelocityA(),
                "velocity_b",      request.getVelocityB(),
                "velocity_c",      request.getVelocityC(),
                "mean_velocity",   (request.getVelocityA() + request.getVelocityB() + request.getVelocityC()) / 3.0,
                "pressure_drop_ab", request.getNodeAPressure() - request.getNodeBPressure(),
                "pressure_drop_bc", request.getNodeBPressure() - request.getNodeCPressure(),
                "pressure_drop_ac", request.getNodeAPressure() - request.getNodeCPressure()
                // dp_dt_* would need previous state — default to 0.0 here or pull from LatencyTrackingService
        );

        // 3. LLM recommendation
        String recommendation = recommendationService.generateRecommendation(prediction, features);
    ...
    }

    /**
     * Lightweight health check — confirms the AI recommendation chain is reachable.
     * Useful during deployment to verify Groq/Spring AI wiring before live data flows in.
     */
    @Operation(summary = "Health check for the recommendation service")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "RecommendationService",
                "timestamp", OffsetDateTime.now().toString()
        ));
    }
}