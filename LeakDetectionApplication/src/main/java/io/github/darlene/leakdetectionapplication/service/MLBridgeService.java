package io.github.darlene.leakdetectionapplication.service;

import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;
import io.github.darlene.leakdetectionapplication.exception.MLPredictionFailedException;
import io.github.darlene.leakdetectionapplication.exception.MLServiceUnavailableException;

import java.util.Map;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j

public class MLBridgeService{

    @Value("${ml-service.base-url}")
    private String mlServiceBaseUrl;

    @Value("${ml-service.timeout-seconds}")
    private int timeoutSeconds;

    private final WebClient.Builder webClientBuilder;

    public MLPredictionResponse predic(Map<String, Double> features){
        try {
            //Create a web client
            WebClient webClient = WebClient.builder()
                    .baseUrl(mlServiceBaseUrl)
                    .build();

            // Post to predict
            MLPredictionResponse response = webClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(features)
                    .retrieve()
                    .bodyToMono(MLPredictionResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .onErrorMap(TimeoutException.class, ex -> new MLServiceUnavailableException("ML service timed out"))
                    .block();

            log.debug("ML Preduction result: {}", response);
            return response;

        } catch (WebClientResponseException ex){
            log.error("ML service returned error response", ex);
            throw new MLPredictionFailedException("ML prediction failed", ex);
        } catch (webCLientException ex){
            log.error("Could not connect to ML service", ex);
            throw new MLServiceUnavailableException("ML service unavailable", ex)
        }
    }


    public boolean isMLServiceHealthy(){
        try{

            WebClient webClient = WebClient.builder()
                    .baseUrl(mlServiceBaseUrl)
                    .build();

            webClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;

        } catch (Exception e){
            return false;
        }
    }
}