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
import io.github.darlene.leakdetectionapplication.dto.response.FaultAlertResponse;
import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;
import io.github.darlene.leakdetectionapplication.exception.ScenarioNotFoundException;
import io.github.darlene.leakdetectionapplication.exception.MLServiceUnavailableException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

/**
 * Core orchestration service for pipeline sensor data processing.
 */
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
        log.debug("Processing reading from device: {}", request.getDeviceId());

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

            log.debug("Flask response: status={} label={} confidence={} progress={}",
                    prediction.getStatus(), prediction.getLabel(),
                    prediction.getConfidence(), prediction.getWindowProgress());

            if (prediction.isCollecting()) {
                log.debug("Device {} collecting window: {}",
                        request.getDeviceId(), prediction.getWindowProgress());
                latencyTrackingService.recordLatency(readingId);
                return;
            }

            double confidencePct = prediction.getConfidence() != null
                    ? prediction.getConfidence() * 100 : 0.0;

            log.debug("Prediction: {} confidence: {:.1f}%",
                    prediction.getPredictedClass(), confidencePct);

            if (!"NORMAL".equalsIgnoreCase(prediction.getPredictedClass())) {

                String recommendation = recommendationService
                        .generateRecommendation(prediction, features);

                long latencyMs = latencyTrackingService.recordLatency(readingId);

                FaultAlertResponse alertResponse = alertService.saveAlert(
                        savedReading, prediction, recommendation, latencyMs);

                String ledColor = resolveLedColor(prediction.getLabel());
                mqttPublisher.publishLedStatus(ledColor);
                alertWebSocketHandler.broadcastAlert(alertResponse);

                log.info("Fault detected: {} confidence: {:.1f}% latency: {}ms",
                        prediction.getPredictedClass(), confidencePct, latencyMs);

            } else {
                latencyTrackingService.recordLatency(readingId);
                mqttPublisher.publishLedStatus("GREEN");
                log.debug("Normal reading - device: {}", request.getDeviceId());
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

    public FaultAlertResponse simulateScenario(String scenarioName) {
        SensorReadingRequest request = buildScenarioRequest(scenarioName);
        processReading(request);
        return alertService.getMostRecentAlert()
                .orElseThrow(() -> new RuntimeException(
                        "No alert generated for scenario: " + scenarioName));
    }

    public FaultAlertResponse injectFault(SimulationRequest request) {
        double[] vals = switch (request.getFaultClass()) {
            case LEAK     -> new double[]{235000.0, 155000.0,  90000.0, 1.1};
            case BLOCKAGE -> new double[]{280000.0, 120000.0, 192000.0, 1.3};
            case NORMAL   -> new double[]{245000.0, 220000.0, 198000.0, 2.1};
        };

        SensorReadingRequest sensorRequest = SensorReadingRequest.builder()
                .deviceId("ESP32_SIM_01")
                .ts(OffsetDateTime.now(ZoneOffset.UTC))
                .nodeAPressure(vals[0]).velocityA(vals[3])
                .nodeBPressure(vals[1]).velocityB(vals[3])
                .nodeCPressure(vals[2]).velocityC(vals[3])
                .scenario(request.getFaultClass().name())
                .build();

        processReading(sensorRequest);

        return alertService.getMostRecentAlert()
                .orElseThrow(() -> new RuntimeException(
                        "No alert generated for fault injection"));
    }

    private SensorReading convertToEntity(SensorReadingRequest request) {
        // ── FIX: use OffsetDateTime to match SensorReading.readingTime field ──
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

    private SensorReadingRequest buildScenarioRequest(String scenarioName) {
        double[] vals = switch (scenarioName) {
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
                .ts(OffsetDateTime.now(ZoneOffset.UTC))
                .nodeAPressure(vals[0]).velocityA(vals[3])
                .nodeBPressure(vals[1]).velocityB(vals[3])
                .nodeCPressure(vals[2]).velocityC(vals[3])
                .scenario(scenarioName)
                .build();
    }

    private String resolveLedColor(String label) {
        if (label == null) return "GREEN";
        return switch (label.toUpperCase()) {
            case "BLOCKAGE" -> "RED";
            case "LEAK"     -> "YELLOW";
            default         -> "GREEN";
        };
    }
}