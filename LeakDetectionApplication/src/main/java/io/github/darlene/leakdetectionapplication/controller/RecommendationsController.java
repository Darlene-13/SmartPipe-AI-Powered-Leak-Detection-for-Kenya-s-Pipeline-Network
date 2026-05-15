package io.github.darlene.leakdetectionapplication.controller;

import io.github.darlene.leakdetectionapplication.dto.request.SensorReadingRequest;
import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;
import io.github.darlene.leakdetectionapplication.dto.response.RecommendationResponse;
import io.github.darlene.leakdetectionapplication.service.FeatureExtractionService;
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

    private final MLBridgeService mlBridgeService;
    private final RecommendationService recommendationService;
    private final FeatureExtractionService featureExtractionService;

    @Operation(summary = "Generate a maintenance recommendation for given sensor readings")
    @PostMapping("/generate")
    public ResponseEntity<RecommendationResponse> generate(
            @Valid @RequestBody SensorReadingRequest request) {

        log.info("Recommendation request received — device: {}", request.getDeviceId());

        // 1. Extract full features map — includes real dp_dt_* via PreviousReadingState
        Map<String, Double> features = featureExtractionService.extractFeatures(request);

        // 2. Get ML prediction from Flask
        MLPredictionResponse prediction = mlBridgeService.predict(request);

        // 3. Generate LLM recommendation using real feature values
        String recommendation = recommendationService.generateRecommendation(prediction, features);

        RecommendationResponse response = RecommendationResponse.builder()
                .deviceId(request.getDeviceId())
                .predictedClass(prediction.getPredictedClass())
                .confidence(prediction.getConfidence())
                .label(prediction.getLabel())
                .recommendation(recommendation)
                .generatedAt(OffsetDateTime.now())
                .build();

        log.info("Recommendation done — class: {}, confidence: {}%",
                prediction.getPredictedClass(),
                prediction.getConfidence() != null ? prediction.getConfidence() * 100 : "n/a");

        return ResponseEntity.ok(response);
    }

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