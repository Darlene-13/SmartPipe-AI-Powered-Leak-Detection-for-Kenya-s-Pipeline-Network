package io.github.darlene.leakdetectionapplication.service;

import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.ChatClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;
import io.github.darlene.leakdetectionapplication.exception.RecommendationServiceException;

import java.util.Map;

/**
 * Service that calls local Ollama LLM via Spring AI to generate
 * human readable maintenance recommendations based on fault type,
 * severity, and all pipeline sensor readings.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ChatClient chatClient;

    /**
     * Generates a maintenance recommendation for a detected fault.
     * Returns immediately for NORMAL predictions without calling LLM.
     * Throws RecommendationServiceException if LLM call fails.
     */
    public String generateRecommendation(
            MLPredictionResponse prediction,
            Map<String, Double> features) {

        if ("NORMAL".equalsIgnoreCase(prediction.getPredictedClass())) {
            return "Pipeline operating normally. No action required.";
        }

        try {
            String prompt = buildPrompt(prediction, features);

            String recommendation = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("Recommendation generated for fault class: {}",
                    prediction.getPredictedClass());

            return recommendation;

        } catch (Exception e) {
            log.error("LLM recommendation failed for class {}: {}",
                    prediction.getPredictedClass(), e.getMessage(), e);
            throw new RecommendationServiceException(
                    "Failed to generate recommendation", e);
        }
    }

    /**
     * Builds a structured prompt string for the LLM.
     * Includes fault class, confidence, severity, all sensor readings,
     * pressure differentials, and rates of pressure change per node.
     */
    private String buildPrompt(
            MLPredictionResponse prediction,
            Map<String, Double> features) {

        String predictedClass = prediction.getPredictedClass() != null
                ? prediction.getPredictedClass() : "UNKNOWN";

        String severity = prediction.getLabel() != null
                ? prediction.getLabel() : "UNSPECIFIED";

        double confidence = prediction.getConfidence() != null
                ? prediction.getConfidence() * 100 : 0.0;

        return String.format("""
                You are an expert pipeline engineer for a copper
                tailings slurry pipeline system.

                FAULT DETECTED: %s
                CONFIDENCE: %.1f%%
                SEVERITY: %s

                CURRENT SENSOR READINGS:
                Node A Pressure: %.2f Pa
                Node B Pressure: %.2f Pa
                Node C Pressure: %.2f Pa
                Flow Velocity:   %.2f m/s

                PRESSURE DIFFERENTIALS:
                A to B drop: %.2f Pa
                B to C drop: %.2f Pa
                A to C drop: %.2f Pa

                RATES OF PRESSURE CHANGE:
                Node A: %.4f Pa/s
                Node B: %.4f Pa/s
                Node C: %.4f Pa/s

                Provide a concise 4-5 point maintenance recommendation
                for the pipeline operator.
                Write in plain English with no em dashes.
                Use point form.
                """,
                predictedClass,
                confidence,
                severity,
                features.getOrDefault("node_a_pressure", 0.0),
                features.getOrDefault("node_b_pressure", 0.0),
                features.getOrDefault("node_c_pressure", 0.0),
                features.getOrDefault("flow_velocity", 0.0),
                features.getOrDefault("pressure_drop_ab", 0.0),
                features.getOrDefault("pressure_drop_bc", 0.0),
                features.getOrDefault("pressure_drop_ac", 0.0),
                features.getOrDefault("dp_dt_a", 0.0),
                features.getOrDefault("dp_dt_b", 0.0),
                features.getOrDefault("dp_dt_c", 0.0)
        );
    }
}