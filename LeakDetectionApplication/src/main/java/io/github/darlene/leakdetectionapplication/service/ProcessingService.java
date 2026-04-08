package io.github.darlene.leakdetectionapplication.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.github.darlene.leakdetectionapplication.mqtt.MqttPublisher;
import io.github.darlene.leakdetectionapplication.websocket.AlertWebSocketHandler;
import io.github.darlene.leakdetectionapplication.repository.SensorReadingRepository;
import io.github.darlene.leakdetectionapplication.dto.request.SensorReadingRequest;
import io.github.darlene.leakdetectionapplication.dto.request.SimulationRequest;
import io.github.darlene.leakdetectionapplication.domain.SensorReading;
import io.github.darlene.leakdetectionapplication.domain.FaultClass;
import io.github.darlene.leakdetectionapplication.service.ScenarioType;
import io.github.darlene.leakdetectionapplication.dto.response.FaultAlertResponse;
import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;
import io.github.darlene.leakdetectionapplication.exception.ScenarioNotFoundException;
import io.github.darlene.leakdetectionapplication.exception.MLServiceUnavailableException;

import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Core orchestration service for pipeline sensor data processing.
 * Called by MqttSubscriber for every incoming sensor reading.
 * Coordinates feature extraction, ML prediction, alert generation,
 * recommendation, WebSocket broadcast and LED status publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingService {

    private final FeatureExtractionService featureExtractionService;
    private final MLBridgeService mlBridgeService;
    private final SensorReadingRepository sensorReadingRepository;
    private final AlertService alertService;
    private final RecommendationService recommendationService;
    private final LatencyTrackingService latencyTrackingService;
    private final MqttPublisher mqttPublisher;
    private final AlertWebSocketHandler alertWebSocketHandler;

    /**
     * Processes a single sensor reading through the full pipeline.
     * Entry point called by MqttSubscriber on every MQTT message received.
     */
    public void processReading(SensorReadingRequest request) {
        String readingId = UUID.randomUUID().toString();
        latencyTrackingService.startTracking(readingId);
        log.debug("Processing reading from device: {}", request.getDeviceId());

        try {
            // Step 1 — Save raw reading
            SensorReading entity = convertToEntity(request);
            Map<String, Double> features = featureExtractionService.extractFeatures(request);
            entity.setDpDtA(features.get("dp_dt_a"));
            entity.setDpDtB(features.get("dp_dt_b"));
            entity.setDpDtC(features.get("dp_dt_c"));
            SensorReading savedReading = sensorReadingRepository.save(entity);

            // Step 2 — ML prediction
            MLPredictionResponse prediction = cacheService
                    .getCachedPrediction(features)
                    .orElseGet(() -> {
                        MLPredictionResponse fresh = mlBridgeService.predict(features);
                        cacheService.cachePrediction(features, fresh);
                        return fresh;
                    });
            log.debug("Prediction: {} confidence: {}%",
                    prediction.getPredictedClass(),
                    prediction.getConfidence() * 100);

            // Step 3 — Handle fault or normal
            if (!"NORMAL".equalsIgnoreCase(prediction.getPredictedClass())) {

                // Step 3a — Generate recommendation
                String recommendation = recommendationService
                        .generateRecommendation(prediction, features);

                // Step 3b — Record latency
                long latencyMs = latencyTrackingService.recordLatency(readingId);

                // Step 3c — Save alert
                FaultAlertResponse alertResponse = alertService.saveAlert(
                        savedReading, prediction, recommendation, latencyMs);

                // Step 3d — Publish LED status to ESP32
                String ledColor = resolveLedColor(prediction.getLabel());
                mqttPublisher.publishLedStatus(ledColor);

                // Step 3e — Broadcast to dashboard
                alertWebSocketHandler.broadcastAlert(alertResponse);

                log.info("Fault processed: {} in {}ms",
                        prediction.getPredictedClass(), latencyMs);

            } else {
                latencyTrackingService.recordLatency(readingId);
                mqttPublisher.publishLedStatus("GREEN");
                log.debug("Normal reading processed for device: {}", request.getDeviceId());
            }

        } catch (MLServiceUnavailableException e) {
            log.error("ML service unavailable for reading: {}", readingId, e);
            latencyTrackingService.recordLatency(readingId);
            mqttPublisher.publishLedStatus("BLUE");
            throw e;

        } catch (Exception e) {
            log.error("Processing failed for reading: {}", readingId, e);
            latencyTrackingService.recordLatency(readingId);
            throw e;
        }
    }

    /**
     * Simulates a named ANSYS scenario through the full processing pipeline.
     * Used by SimulationController for HIL validation and demo.
     */
    public FaultAlertResponse simulateScenario(String scenarioName) {
        SensorReadingRequest request = buildScenarioRequest(scenarioName);
        processReading(request);
        return alertService.getMostRecentAlert()
                .orElseThrow(() -> new RuntimeException(
                        "No alert generated for scenario: " + scenarioName));
    }

    /**
     * Injects a fault based on SimulationRequest from dashboard.
     * Used by SimulationController for manual fault injection.
     */
    public FaultAlertResponse injectFault(SimulationRequest request) {

        double[] pressures = switch (request.getFaultClass()) {
            case LEAK ->     new double[]{235000.0, 155000.0,  90000.0, 1.1};
            case BLOCKAGE -> new double[]{280000.0, 120000.0, 192000.0, 1.3};
            case NORMAL ->   new double[]{245000.0, 220000.0, 198000.0, 2.1};
        };

        SensorReadingRequest sensorRequest = SensorReadingRequest.builder()
                .deviceId("ESP32_SIM_01")
                .timestamp(LocalDateTime.now())
                .nodeAPressure(pressures[0])
                .nodeBPressure(pressures[1])
                .nodeCPressure(pressures[2])
                .flowVelocity(pressures[3])
                .scenario(request.getFaultClass().name())
                .build();

        processReading(sensorRequest);

        return alertService.getMostRecentAlert()
                .orElseThrow(() -> new RuntimeException(
                        "No alert generated for fault injection"));
    }

    /**
     * Converts incoming MQTT request DTO to SensorReading entity.
     * dpDt values set separately after feature extraction.
     */
    private SensorReading convertToEntity(SensorReadingRequest request) {
        return SensorReading.builder()
                .deviceId(request.getDeviceId())
                .timestamp(request.getTimestamp())
                .nodeAPressure(request.getNodeAPressure())
                .nodeBPressure(request.getNodeBPressure())
                .nodeCPressure(request.getNodeCPressure())
                .flowVelocity(request.getFlowVelocity())
                .scenario(request.getScenario())
                .build();
    }

    /**
     * Builds a SensorReadingRequest from a named ANSYS scenario.
     * Throws ScenarioNotFoundException if scenario name is invalid.
     */
    private SensorReadingRequest buildScenarioRequest(String scenarioName) {

        double[] pressures = switch (scenarioName) {
            case "NORMAL_BASELINE" -> new double[]{245000.0, 220000.0, 198000.0, 2.1};
            case "LEAK_INCIPIENT"  -> new double[]{243000.0, 198000.0, 165000.0, 1.9};
            case "LEAK_MODERATE"   -> new double[]{240000.0, 180000.0, 130000.0, 1.6};
            case "LEAK_CRITICAL"   -> new double[]{235000.0, 155000.0,  90000.0, 1.1};
            case "BLOCKAGE_25"     -> new double[]{260000.0, 155000.0, 195000.0, 1.7};
            case "BLOCKAGE_50"     -> new double[]{280000.0, 120000.0, 192000.0, 1.3};
            case "BLOCKAGE_75"     -> new double[]{310000.0,  80000.0, 188000.0, 0.8};
            default -> throw new ScenarioNotFoundException(
                    "Scenario not found: " + scenarioName);
        };

        return SensorReadingRequest.builder()
                .deviceId("ESP32_SIM_01")
                .timestamp(LocalDateTime.now())
                .nodeAPressure(pressures[0])
                .nodeBPressure(pressures[1])
                .nodeCPressure(pressures[2])
                .flowVelocity(pressures[3])
                .scenario(scenarioName)
                .build();
    }

    /**
     * Resolves LED color string from severity label.
     * Published back to ESP32 via MQTT after fault classification.
     */
    private String resolveLedColor(String severityLabel) {
        return switch (severityLabel) {
            case "CRITICAL" -> "RED";
            case "MODERATE" -> "YELLOW";
            case "LOW"      -> "YELLOW";
            default         -> "GREEN";
        };
    }
}