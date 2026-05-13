package io.github.darlene.leakdetectionapplication.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import io.github.darlene.leakdetectionapplication.dto.request.SensorReadingRequest;
import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;
import io.github.darlene.leakdetectionapplication.exception.MLPredictionFailedException;
import io.github.darlene.leakdetectionapplication.exception.MLServiceUnavailableException;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Service responsible for communicating with the Python Flask ML service.
 *
 * Flask /predict expects:
 * {
 *   "device_id":        "ESP32_NODE_01",
 *   "node_a_pressure":  36180.0,
 *   "velocity_a":       2.48,
 *   "node_b_pressure":  20086.0,
 *   "velocity_b":       2.48,
 *   "node_c_pressure":  3977.0,
 *   "velocity_c":       2.48
 * }
 *
 * NOTE: Flask preprocessor handles all feature engineering internally.
 * Spring Boot sends only the raw 6 sensor values + device_id.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MLBridgeService {

    @Value("${ml-service.base-url}")
    private String mlServiceBaseUrl;

    @Value("${ml-service.timeout-seconds}")
    private int timeoutSeconds;

    private final WebClient.Builder webClientBuilder;
    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = webClientBuilder
                .baseUrl(mlServiceBaseUrl)
                .build();
        log.info("MLBridgeService initialized — base URL: {}", mlServiceBaseUrl);
    }

    /**
     * Sends raw sensor reading to Flask ML service.
     * Flask preprocessor does its own feature engineering — we only send
     * the 6 raw sensor values + device_id that Flask /predict expects.
     *
     * Returns collecting status when window not yet full (first 9 readings).
     * Returns full prediction on reading 10+.
     */
    public MLPredictionResponse predict(SensorReadingRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("device_id",       request.getDeviceId());
        payload.put("node_a_pressure", request.getNodeAPressure());
        payload.put("velocity_a",      request.getVelocityA());
        payload.put("node_b_pressure", request.getNodeBPressure());
        payload.put("velocity_b",      request.getVelocityB());
        payload.put("node_c_pressure", request.getNodeCPressure());
        payload.put("velocity_c",      request.getVelocityC());

        try {
            MLPredictionResponse response = webClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(MLPredictionResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .onErrorMap(TimeoutException.class, ex ->
                            new MLServiceUnavailableException(
                                    "ML service timed out after " + timeoutSeconds + "s", ex))
                    .block();

            if (response == null) {
                throw new MLPredictionFailedException("ML service returned null response");
            }

            log.debug("ML response: status={} label={} confidence={}%",
                    response.getStatus(),
                    response.getLabel(),
                    response.getConfidence() != null ? response.getConfidence() * 100 : "n/a");

            return response;

        } catch (WebClientResponseException ex) {
            log.error("ML service error response: status={} body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new MLPredictionFailedException(
                    "ML prediction failed: " + ex.getStatusCode(), ex);
        } catch (WebClientException ex) {
            log.error("Cannot connect to ML service at {}", mlServiceBaseUrl, ex);
            throw new MLServiceUnavailableException(
                    "ML service unavailable at " + mlServiceBaseUrl, ex);
        }
    }

    @Scheduled(fixedDelay = 600000) // ping every 10 minutes
    public void keepMLServiceAwake() {
        try {
            webClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(5));
            log.debug("ML service keep-alive ping OK");
        } catch (Exception e) {
            log.warn("ML service keep-alive ping failed: {}", e.getMessage());
        }
    }

    /**
     * Health check — used by StatusController.
     */
    public boolean isMLServiceHealthy() {
        try {
            webClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(5));
            return true;
        } catch (Exception e) {
            log.warn("ML service health check failed: {}", e.getMessage());
            return false;
        }
    }
}