package io.github.darlene.leakdetectionapplication.service;

// Spring
import org.springframework.stereotype.Service;

import io.github.darlene.leakdetectionapplication.dto.response.LatencyStatsResponse;

// Lombok
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Java
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;
import java.util.List;


/**
 * Service for tracking end to end processing latency.
 * Measures time from MQTT message receipt to alert persistence.
 * Used to validate the less than 5 second latency requirement.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class LatencyTrackingService{
    // Thread-safe map to store start times
    private Map<String, Instant> startTimes = new ConcurrentHashMap<>();

    // Thread-safe list to store latency history (in milliseconds)
    private List<Long> latencyHistory = new CopyOnWriteArrayList<>();

    // Method to start tracking
    public void startTracking(String readingId){
        //Store the current instant in startTimes map with readingId as key
        startTimes.put(readingId, Instant.now());
        log.debug("Started tracking readingId: {}", readingId);

    }

    // Record latency
    public long recordLatency(String readingId){
        // Get start time from map using readingId
        Instant startTime = startTimes.get(readingId);

        if(startTime == null){
            log.warn("No start time found for readingId : {}", readingId);
            return 0L;
        }

        long elapsedMillis = Instant.now().toEpochMilli() - startTime.toEpochMilli();
        startTimes.remove(readingId);
        latencyHistory.add(elapsedMillis);
        log.debug("Latency for readingId {}: {}ms", readingId, elapsedMillis);
        return elapsedMillis;
    }

    // Get average latency
    public double getAverageLatency(){
        return latencyHistory.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }

    //Get minimum latency
    public long getMinLatency(){
        if(latencyHistory.isEmpty()){
            return 0L;
        }

        return latencyHistory.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0L);

    }
    // Get maximum latency
    public long getMaxLatency(){
        if(latencyHistory.isEmpty()){
            return 0L;
        }

        return latencyHistory.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
    }

    // Get latency stats ...all present data types in latency stats response
    public LatencyStatsResponse getLatencyStatsResponse(){
        return LatencyStatsResponse.builder()
                .averageLatency(getAverageLatency())
                .minLatency(getMinLatency())
                .maxLatency(getMaxLatency())
                .totalRequests(latencyHistory.size())
                .lastMeasuredAt(LocalDateTime.now())
                .build();
    }


    // Clear history
    public void clearHistory(){
        latencyHistory.clear();
        startTimes.clear();
    }

}