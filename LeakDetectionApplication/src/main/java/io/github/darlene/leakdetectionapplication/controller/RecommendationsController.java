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
    @Operation(summary = "Generate a maintenance recommendation for given sensor readings")
    @PostMapping("/generate")
    public ResponseEntity<RecommendationResponse> generate(
            @Valid @RequestBody RecommendationRequest request) {

        log.info("Recommendation request received for pipeline segment: {}",
                request.getPipelineSegment());

        Map<String, Double> features = request.getFeatures();

        // 1. Call Flask ML service for prediction
        MLPredictionResponse prediction = mlBridgeService.predict(features);

        // 2. Pass prediction + raw features to LLM service
        String recommendation = recommendationService.generateRecommendation(prediction, features);

        RecommendationResponse response = RecommendationResponse.builder()
                .pipelineSegment(request.getPipelineSegment())
                .predictedClass(prediction.getPredictedClass())
                .confidence(prediction.getConfidence())
                .label(prediction.getLabel())
                .recommendation(recommendation)
                .generatedAt(OffsetDateTime.now())
                .build();

        log.info("Recommendation generated — class: {}, confidence: {:.1f}%",
                prediction.getPredictedClass(), prediction.getConfidence() * 100);

        return ResponseEntity.ok(response);
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