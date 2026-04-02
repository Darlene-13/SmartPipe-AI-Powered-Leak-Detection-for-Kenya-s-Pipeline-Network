package io.github.darlene.leakdetectionapplication.service;

import io.github.darlene.leakdetectionapplication.dto.request.SensorReadingRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for extracting ML features from raw sensor readings.
 * Calculates pressure rates of change and differential pressure drops
 * across pipeline nodes for fault classification.
 */
@Service
@Slf4j
public class FeatureExtractionService {

    private final Map<String, PreviousReadingState> previousReadings = new ConcurrentHashMap<>();

    /**
     * Extracts feature vector from sensor reading for ML model input.
     * Calculates dp/dt per node and inter-node pressure drops.
     *
     * @param request incoming sensor reading
     * @return map of feature names to numeric values
     */
    public Map<String, Double> extractFeatures(SensorReadingRequest request) {

        String deviceId = request.getDeviceId();
        Instant now = Instant.now();

        PreviousReadingState previous = previousReadings.get(deviceId);

        double dpDtA = 0.0;
        double dpDtB = 0.0;
        double dpDtC = 0.0;

        // Calculate dp/dt if previous reading exists
        if (previous != null && previous.getTimestamp() != null) {
            long timeDeltaMillis = Duration.between(previous.getTimestamp(), now).toMillis();

            if (timeDeltaMillis > 0) {
                double timeDeltaSeconds = timeDeltaMillis / 1000.0;

                dpDtA = (request.getNodeAPressure() - previous.getPressureA()) / timeDeltaSeconds;
                dpDtB = (request.getNodeBPressure() - previous.getPressureB()) / timeDeltaSeconds;
                dpDtC = (request.getNodeCPressure() - previous.getPressureC()) / timeDeltaSeconds;
            }
        }

        // Build features map
        Map<String, Double> features = new HashMap<>();
        features.put("node_a_pressure", request.getNodeAPressure());
        features.put("node_b_pressure", request.getNodeBPressure());
        features.put("node_c_pressure", request.getNodeCPressure());
        features.put("flow_velocity", request.getFlowVelocity());
        features.put("dp_dt_a", dpDtA);
        features.put("dp_dt_b", dpDtB);
        features.put("dp_dt_c", dpDtC);
        features.put("pressure_drop_ab", request.getNodeAPressure() - request.getNodeBPressure());
        features.put("pressure_drop_bc", request.getNodeBPressure() - request.getNodeCPressure());
        features.put("pressure_drop_ac", request.getNodeAPressure() - request.getNodeCPressure());

        // Update previous readings AFTER feature extraction
        PreviousReadingState currentState = new PreviousReadingState(
                request.getNodeAPressure(),
                request.getNodeBPressure(),
                request.getNodeCPressure(),
                now
        );
        previousReadings.put(deviceId, currentState);

        log.debug("Extracted features for device {}: {}", deviceId, features);

        return features;
    }

    /**
     * Convenience method returning only the Node A pressure rate of change.
     * Used when only dp/dt is required.
     * @param request incoming sensor reading
     * @return Node A dp/dt value
     */
    public Double getDpDt(SensorReadingRequest request) {
        return extractFeatures(request).get("dp_dt_a");
    }

    @Getter
    @AllArgsConstructor
    private static class PreviousReadingState {
        private final double pressureA;
        private final double pressureB;
        private final double pressureC;
        private final Instant timestamp;
    }
}
