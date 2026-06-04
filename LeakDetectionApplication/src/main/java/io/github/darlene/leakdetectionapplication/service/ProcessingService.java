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
import io.github.darlene.leakdetectionapplication.dto.response.FaultAlertResponse;
import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;
import io.github.darlene.leakdetectionapplication.exception.ScenarioNotFoundException;
import io.github.darlene.leakdetectionapplication.exception.MLServiceUnavailableException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingService {

    private final FeatureExtractionService featureExtractionService;
    private final MLBridgeService          mlBridgeService;
    private final SensorReadingRepository  sensorReadingRepository;
    private final AlertService             alertService;
    private final RecommendationService    recommendationService;
    private final LatencyTrackingService   latencyTrackingService;
    private final MqttPublisher            mqttPublisher;
    private final AlertWebSocketHandler    alertWebSocketHandler;
    private final CacheService             cacheService;

    public void processReading(SensorReadingRequest request) {
        String readingId = UUID.randomUUID().toString();
        latencyTrackingService.startTracking(readingId);

        try {
            Map<String, Double> features = featureExtractionService.extractFeatures(request);

            SensorReading entity = convertToEntity(request);
            entity.setDpDtA(features.get("dp_dt_a"));
            entity.setDpDtB(features.get("dp_dt_b"));
            entity.setDpDtC(features.get("dp_dt_c"));
            SensorReading savedReading = sensorReadingRepository.save(entity);

            MLPredictionResponse prediction = cacheService
                    .getCachedPrediction(features)
                    .orElseGet(() -> {
                        MLPredictionResponse fresh = mlBridgeService.predict(request);
                        cacheService.cachePrediction(features, fresh);
                        return fresh;
                    });

            if (prediction.isCollecting()) {
                log.debug("Device {} collecting window: {}", request.getDeviceId(), prediction.getWindowProgress());
                latencyTrackingService.recordLatency(readingId);
                return;
            }

            double confidencePct = prediction.getConfidence() != null ? prediction.getConfidence() * 100 : 0.0;

            if (!"NORMAL".equalsIgnoreCase(prediction.getPredictedClass())) {

                String recommendation;
                try {
                    recommendation = recommendationService.generateRecommendation(prediction, features);
                } catch (Exception e) {
                    log.warn("LLM unavailable, using fallback: {}", e.getMessage());
                    recommendation = prediction.getPredictedClass() + " detected - operator action required";
                }

                long latencyMs = latencyTrackingService.recordLatency(readingId);
                FaultAlertResponse alertResponse = alertService.saveAlert(savedReading, prediction, recommendation, latencyMs);

                // ── LED publish never crashes the pipeline ──
                try {
                    mqttPublisher.publishLedStatus(resolveLedColor(prediction.getLabel()));
                } catch (Exception e) {
                    log.warn("LED publish failed (continuing): {}", e.getMessage());
                }

                // ── WebSocket broadcast always fires ──
                alertWebSocketHandler.broadcastAlert(alertResponse);

                log.info("Fault detected: {} confidence: {}% latency: {}ms",
                        prediction.getPredictedClass(), confidencePct, latencyMs);

            } else {
                latencyTrackingService.recordLatency(readingId);
                try {
                    mqttPublisher.publishLedStatus("GREEN");
                } catch (Exception e) {
                    log.warn("LED publish failed (continuing): {}", e.getMessage());
                }
            }

        } catch (MLServiceUnavailableException e) {
            log.error("ML service unavailable for reading: {}", readingId, e);
            latencyTrackingService.recordLatency(readingId);
            throw e;
        } catch (Exception e) {
            log.error("Processing failed for reading: {}", readingId, e);
            latencyTrackingService.recordLatency(readingId);
            throw e;
        }
    }

    /**
     * Scenario grid button — fetches 100 real readings from DB
     * for that scenario and processes them so ML window fills correctly.
     */
    public FaultAlertResponse simulateScenario(String scenarioName) {
        // Fetch 100 real readings from DB matching this scenario
        List<SensorReading> readings = sensorReadingRepository
                .findTop100ByScenarioContainingIgnoreCaseOrderByReadingTimeAsc(
                        scenarioToDbPattern(scenarioName));

        if (readings.isEmpty()) {
            throw new ScenarioNotFoundException(
                    "No readings found in DB for scenario: " + scenarioName);
        }

        log.info("Simulating scenario: {} using {} real DB readings", scenarioName, readings.size());

        for (SensorReading r : readings) {
            SensorReadingRequest req = SensorReadingRequest.builder()
                    .deviceId("ESP32_SIM_01")
                    .ts(OffsetDateTime.now(ZoneOffset.UTC))
                    .nodeAPressure(r.getNodeAPressure())
                    .velocityA(r.getVelocityA())
                    .nodeBPressure(r.getNodeBPressure())
                    .velocityB(r.getVelocityB())
                    .nodeCPressure(r.getNodeCPressure())
                    .velocityC(r.getVelocityC())
                    .scenario(r.getScenario())
                    .build();
            try {
                processReading(req);
            } catch (Exception e) {
                log.warn("Simulation reading failed: {}", e.getMessage());
            }
        }

        return alertService.getMostRecentAlert()
                .orElseThrow(() -> new RuntimeException(
                        "No alert generated for scenario: " + scenarioName));
    }

    /**
     * Manual fault injection — same logic, uses faultClass to pick scenario pattern.
     */
    public FaultAlertResponse injectFault(SimulationRequest request) {
        String pattern = switch (request.getFaultClass()) {
            case LEAK     -> "leak";
            case BLOCKAGE -> "blockage";
            case NORMAL   -> "normal";
        };

        List<SensorReading> readings = sensorReadingRepository
                .findTop100ByScenarioContainingIgnoreCaseOrderByReadingTimeAsc(pattern);

        if (readings.isEmpty()) {
            throw new ScenarioNotFoundException(
                    "No readings found in DB for fault class: " + request.getFaultClass());
        }

        log.info("Injecting fault: {} using {} real DB readings", request.getFaultClass(), readings.size());

        for (SensorReading r : readings) {
            SensorReadingRequest req = SensorReadingRequest.builder()
                    .deviceId("ESP32_SIM_01")
                    .ts(OffsetDateTime.now(ZoneOffset.UTC))
                    .nodeAPressure(r.getNodeAPressure())
                    .velocityA(r.getVelocityA())
                    .nodeBPressure(r.getNodeBPressure())
                    .velocityB(r.getVelocityB())
                    .nodeCPressure(r.getNodeCPressure())
                    .velocityC(r.getVelocityC())
                    .scenario(r.getScenario())
                    .build();
            try {
                processReading(req);
            } catch (Exception e) {
                log.warn("Injection reading failed: {}", e.getMessage());
            }
        }

        return alertService.getMostRecentAlert()
                .orElseThrow(() -> new RuntimeException(
                        "No alert generated for fault injection"));
    }

    /**
     * Maps scenario button name to DB scenario pattern.
     * e.g. "LEAK_INCIPIENT" → "leak_incipient"
     *      "BLOCKAGE_75"    → "blockage_75"
     *      "NORMAL_BASELINE"→ "normal"
     */
    private String scenarioToDbPattern(String scenarioName) {
        return scenarioName.toLowerCase().replace("_baseline", "");
    }

    private SensorReading convertToEntity(SensorReadingRequest request) {
        OffsetDateTime readingTime = request.getReadingTime() != null
                ? request.getReadingTime()
                : OffsetDateTime.now(ZoneOffset.UTC);

        return SensorReading.builder()
                .deviceId(request.getDeviceId())
                .readingTime(readingTime)
                .nodeAPressure(request.getNodeAPressure())
                .velocityA(request.getVelocityA())
                .nodeBPressure(request.getNodeBPressure())
                .velocityB(request.getVelocityB())
                .nodeCPressure(request.getNodeCPressure())
                .velocityC(request.getVelocityC())
                .scenario(request.getScenario())
                .build();
    }

    private String resolveLedColor(String label) {
        if (label == null) return "GREEN";
        return switch (label.toUpperCase()) {
            case "BLOCKAGE" -> "YELLOW";
            case "LEAK"     -> "RED";
            default         -> "GREEN";
        };
    }
}