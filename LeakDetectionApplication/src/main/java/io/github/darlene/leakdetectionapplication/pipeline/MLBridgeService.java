package io.github.darlene.leakdetectionapplication.pipeline;

import io.github.darlene.leakdetectionapplication.alert.*;
import io.github.darlene.leakdetectionapplication.analytics.*;
import io.github.darlene.leakdetectionapplication.auth.*;
import io.github.darlene.leakdetectionapplication.configuration.*;
import io.github.darlene.leakdetectionapplication.messaging.*;
import io.github.darlene.leakdetectionapplication.monitoring.*;
import io.github.darlene.leakdetectionapplication.pipeline.*;
import io.github.darlene.leakdetectionapplication.recommendation.*;
import io.github.darlene.leakdetectionapplication.sensor.*;
import io.github.darlene.leakdetectionapplication.simulation.*;
import io.github.darlene.leakdetectionapplication.shared.*;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import io.github.darlene.leakdetectionapplication.sensor.SensorReading;
import io.github.darlene.leakdetectionapplication.pipeline.MLPredictionResponse;
import io.github.darlene.leakdetectionapplication.pipeline.MLPredictionFailedException;
import io.github.darlene.leakdetectionapplication.pipeline.MLServiceUnavailableException;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

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

    public MLPredictionResponse predict(SensorReading entity) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("device_id",       entity.getDeviceId());
        payload.put("node_a_pressure", entity.getNodeAPressure());
        payload.put("velocity_a",      entity.getVelocityA());
        payload.put("node_b_pressure", entity.getNodeBPressure());
        payload.put("velocity_b",      entity.getVelocityB());
        payload.put("node_c_pressure", entity.getNodeCPressure());
        payload.put("velocity_c",      entity.getVelocityC());

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

    @Scheduled(fixedDelay = 600000)
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