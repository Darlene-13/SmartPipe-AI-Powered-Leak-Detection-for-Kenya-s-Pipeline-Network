package io.github.darlene.leakdetectionapplication.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Redis cache service for ML prediction results.
 * Caches predictions by feature vector signature to reduce
 * repeated HTTP calls to the Python ML service.
 * Directly supports the less than 5 second latency requirement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, MLPredictionResponse> redisTemplate;

    @Value("${redis.cache.ttl-seconds}")
    private long ttlSeconds;

    private static final String PREDICTION_KEY_PREFIX = "prediction:";

    /**
     * Retrieves a cached ML prediction by feature vector signature.
     * Returns empty Optional if no cached result exists or cache miss.
     */
    public Optional<MLPredictionResponse> getCachedPrediction(
            Map<String, Double> features) {

        String key = buildCacheKey(features);

        try {
            MLPredictionResponse cached = redisTemplate.opsForValue().get(key);

            if (cached != null) {
                log.debug("Cache HIT for key: {}", key);
                return Optional.of(cached);
            }

            log.debug("Cache MISS for key: {}", key);
            return Optional.empty();

        } catch (Exception e) {
            log.warn("Redis cache read failed — proceeding without cache: {}",
                    e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Stores an ML prediction result in Redis cache.
     * TTL configured via redis.cache.ttl-seconds in application.yml.
     * Fails silently — cache write failure never breaks the main flow.
     */
    public void cachePrediction(
            Map<String, Double> features,
            MLPredictionResponse prediction) {

        String key = buildCacheKey(features);

        try {
            redisTemplate.opsForValue().set(
                    key,
                    prediction,
                    Duration.ofSeconds(ttlSeconds));

            log.debug("Cached prediction for key: {} TTL: {}s", key, ttlSeconds);

        } catch (Exception e) {
            log.warn("Redis cache write failed — prediction not cached: {}",
                    e.getMessage());
        }
    }

    /**
     * Evicts a specific prediction from cache by feature vector.
     * Used when a new scenario overrides a previously cached result.
     */
    public void evictPrediction(Map<String, Double> features) {
        String key = buildCacheKey(features);

        try {
            redisTemplate.delete(key);
            log.debug("Evicted cache entry for key: {}", key);

        } catch (Exception e) {
            log.warn("Redis cache eviction failed: {}", e.getMessage());
        }
    }

    /**
     * Clears all prediction cache entries.
     * Used by SimulationController before running a new scenario.
     */
    public void clearAllPredictions() {
        try {
            var keys = redisTemplate.keys(PREDICTION_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Cleared {} prediction cache entries", keys.size());
            }
        } catch (Exception e) {
            log.warn("Redis cache clear failed: {}", e.getMessage());
        }
    }

    /**
     * Checks if Redis is reachable and responding.
     * Used by StatusController for system health reporting.
     */
    public boolean isRedisHealthy() {
        try {
            redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            return true;
        } catch (Exception e) {
            log.warn("Redis health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Builds a deterministic cache key from the feature vector.
     * Rounds pressure values to nearest 1000 Pa and velocity to 1 decimal
     * to allow cache hits for near-identical readings from the pipeline.
     */
    private String buildCacheKey(Map<String, Double> features) {
        long nodeA = Math.round(
                features.getOrDefault("node_a_pressure", 0.0) / 1000) * 1000;
        long nodeB = Math.round(
                features.getOrDefault("node_b_pressure", 0.0) / 1000) * 1000;
        long nodeC = Math.round(
                features.getOrDefault("node_c_pressure", 0.0) / 1000) * 1000;
        long flow = Math.round(
                features.getOrDefault("flow_velocity", 0.0) * 10);

        return PREDICTION_KEY_PREFIX + nodeA + ":" + nodeB + ":" + nodeC + ":" + flow;
    }
}