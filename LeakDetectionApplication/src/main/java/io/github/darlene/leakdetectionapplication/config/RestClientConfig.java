package io.github.darlene.leakdetectionapplication.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * REST client configuration.
 * Provides RestClient bean for MLBridgeService HTTP calls to Python Flask.
 * Provides ObjectMapper bean for JSON serialization across the application.
 * Timeouts configured via environment variables with sensible defaults.
 */
@Configuration
public class RestClientConfig {

    @Value("${ml.service.url:http://localhost:5000}")
    private String mlServiceUrl;

    @Value("${ml.service.connect-timeout-ms:3000}")
    private int connectTimeoutMs;

    @Value("${ml.service.read-timeout-ms:5000}")
    private int readTimeoutMs;

    @Bean
    public RestClient restClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();

        return RestClient.builder()
                .baseUrl(mlServiceUrl)
                .requestFactory(
                        new org.springframework.http.client.JdkClientHttpRequestFactory(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Registers ObjectMapper as a Spring bean.
     * findAndRegisterModules() auto-registers JavaTimeModule
     * so LocalDateTime fields serialize correctly to JSON.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}