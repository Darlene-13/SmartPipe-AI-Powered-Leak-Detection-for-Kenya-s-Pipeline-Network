package io.github.darlene.leakdetectionapplication.service;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;


import java.util.Duraion;
import java.util.Map;
import java.util.Optional;

/**
 * Redis cache service for ML Prediction results.
 * Caches predictions by feature vector signature to reduce
 * Repeated  HTTP calls to the python ML Service
 *
 */


@Service
public class CacheService{

    private final RedisTemplate<String, MLPredictionResponse> redisTemplate;

    @Value("${redis.cache.ttl-seconds}")
    private long ttlSeconds;

    /**
     *  Retrieve a cached ML prediction feature vector signature
     *  Returns empty Optional if no cached results exists or cache miss
     */

    public Optional<MLPredictionResponse> getCachedPrediction(Map<String, Double> features){
        String key = buildCacheKey(features);\

        try {
            MLPredictionReponse cached = redisTemplate.opsForValue().get(key);


            if (cached ! = null){
                log.debug("Cache HIT for key: {}", key);
                return Optional.of(cached);
            }

            log.debug("Cache MISS for key: {}", key);
            return Optional.empty();

        } catch (Exception e){
            log.warn("Redis cache read failed - proceeding without cache: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Method to store ML Prediction results in redis cache
     * TTL configured via redis.cache.ttl-seconds in application.yml
     * Fails silently - cache write failure should never break the main flow
     * 
     */


    /**
     * Build a deterministic cache key from the feature vector
     * Rounds pressure values to the nearest 1000 pa and velocity to 1 decimal.
     *
     */


    private string buildCacheKey(Map<String, Double> features){
        long nodeA = Math.round(
                features.getOrDefault("node_a_pressure", 0.0) / 1000) * 1000;
        long nodeB  = Math.round(
                features.getOrDefault("node_b_pressure", 0.0) / 1000) * 10000;
        long nodeC = Math.round(
                features.getOrDefault("node_c_pressure", 0.0) / 1000) * 10000;
        long flow = Math.round( features.getOrDefault("flow_velocity", 0.0) * 10);

        return PREDICTION_KET_PREFIX + nodeA + ":" + nodeB + ":" + nodeC + ":" + flow;
    }

}