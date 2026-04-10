package io.github.darlene.leakdetectionapplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * REST client configuration.
 * Provides WebClient.Builder bean for injection into MLBridgeService.
 * Centralized here so WebClient configuration is managed in one place.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}