package io.github.darlene.leakdetectionapplication.sensor;

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

import io.github.darlene.leakdetectionapplication.sensor.SensorReading;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class FeatureExtractionService {

    private final Map<String, PreviousReadingState> previousReadings = new ConcurrentHashMap<>();

    public Map<String, Double> extractFeatures(SensorReading entity) {

        String deviceId = entity.getDeviceId();
        Instant now = Instant.now();

        PreviousReadingState previous = previousReadings.get(deviceId);

        double dpDtA = 0.0;
        double dpDtB = 0.0;
        double dpDtC = 0.0;

        if (previous != null && previous.getTimestamp() != null) {
            long timeDeltaMillis = Duration.between(previous.getTimestamp(), now).toMillis();

            if (timeDeltaMillis > 0) {
                double timeDeltaSeconds = timeDeltaMillis / 1000.0;
                dpDtA = (entity.getNodeAPressure() - previous.getPressureA()) / timeDeltaSeconds;
                dpDtB = (entity.getNodeBPressure() - previous.getPressureB()) / timeDeltaSeconds;
                dpDtC = (entity.getNodeCPressure() - previous.getPressureC()) / timeDeltaSeconds;
            }
        }

        Map<String, Double> features = new HashMap<>();
        features.put("node_a_pressure",  entity.getNodeAPressure());
        features.put("node_b_pressure",  entity.getNodeBPressure());
        features.put("node_c_pressure",  entity.getNodeCPressure());
        features.put("velocity_a",       entity.getVelocityA());
        features.put("velocity_b",       entity.getVelocityB());
        features.put("velocity_c",       entity.getVelocityC());
        features.put("mean_velocity",    (entity.getVelocityA() + entity.getVelocityB() + entity.getVelocityC()) / 3.0);
        features.put("dp_dt_a",          dpDtA);
        features.put("dp_dt_b",          dpDtB);
        features.put("dp_dt_c",          dpDtC);
        features.put("pressure_drop_ab", entity.getNodeAPressure() - entity.getNodeBPressure());
        features.put("pressure_drop_bc", entity.getNodeBPressure() - entity.getNodeCPressure());
        features.put("pressure_drop_ac", entity.getNodeAPressure() - entity.getNodeCPressure());

        previousReadings.put(deviceId, new PreviousReadingState(
                entity.getNodeAPressure(),
                entity.getNodeBPressure(),
                entity.getNodeCPressure(),
                now
        ));

        log.debug("Extracted features for device {}: {}", deviceId, features);

        return features;
    }

    public Double getDpDt(SensorReading entity) {
        return extractFeatures(entity).get("dp_dt_a");
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