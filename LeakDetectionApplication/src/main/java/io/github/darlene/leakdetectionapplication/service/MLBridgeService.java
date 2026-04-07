package io.github.darlene.leakdetectionapplication.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;
import io.github.darlene.leakdetectionapplication.exception.MLPredictionFailedException;
import io.github.darlene.leakdetectionapplication.exception.MLServiceUnavailableException;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Service responsible for communicating with the Python Flask ML service.
 * Sends extracted feature vectors and receives fault classification predictions.
 * Uses WebClient for non-blocking HTTP communication.
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
        log.info("MLBridgeService initialized with base URL: {}", mlServiceBaseUrl);
    }

    /**
     * Sends feature vector to Python ML service and returns fault prediction.
     * Throws MLServiceUnavailableException if service is unreachable or times out.
     * Throws MLPredictionFailedException if service returns an error response.
     */
    public MLPredictionResponse predict(Map<String, Double> features) {
        try {
            MLPredictionResponse response = webClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(features)
                    .retrieve()
                    .bodyToMono(MLPredictionResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .onErrorMap(TimeoutException.class, ex ->
                            new MLServiceUnavailableException(
                                    "ML service timed out after " + timeoutSeconds + " seconds", ex))
                    .block();

            log.debug("ML prediction result: {} confidence: {}%",
                    response != null ? response.getPredictedClass() : "null",
                    response != null ? response.getConfidence() * 100 : 0);

            return response;

        } catch (WebClientResponseException ex) {
            log.error("ML service returned error response: status {}",
                    ex.getStatusCode(), ex);
            throw new MLPredictionFailedException(
                    "ML prediction failed with status: " + ex.getStatusCode(), ex);

        } catch (WebClientException ex) {
            log.error("Could not connect to ML service at {}", mlServiceBaseUrl, ex);
            throw new MLServiceUnavailableException(
                    "ML service unavailable at " + mlServiceBaseUrl, ex);
        }
    }

    /**
     * Checks if the Python ML service is reachable and healthy.
     * Used by StatusController to report ML service connectivity.
     */
    public boolean isMLServiceHealthy() {
        try {
            webClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.debug("ML service health check passed");
            return true;
        } catch (Exception e) {
            log.warn("ML service health check failed: {}", e.getMessage());
            return false;
        }
    }
}