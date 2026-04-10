package io.github.darlene.leakdetectionapplication.service;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.github.darlene.leakdetectionapplication.repository.FaultAlertRepository;
import io.github.darlene.leakdetectionapplication.domain.FaultAlert;
import io.github.darlene.leakdetectionapplication.domain.FaultClass;
import io.github.darlene.leakdetectionapplication.domain.SensorReading;
import io.github.darlene.leakdetectionapplication.domain.SeverityLevel;
import io.github.darlene.leakdetectionapplication.mapper.FaultAlertMapper;
import io.github.darlene.leakdetectionapplication.dto.response.FaultAlertResponse;
import io.github.darlene.leakdetectionapplication.dto.response.AnalyticsSummaryResponse;
import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;
import io.github.darlene.leakdetectionapplication.exception.FaultAlertNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for fault alert persistence and retrieval.
 * Handles alert creation, querying, and analytics summary generation.
 * All pressure statistics calculated across all three pipeline nodes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final FaultAlertRepository faultAlertRepository;
    private final FaultAlertMapper faultAlertMapper;
    private final SensorReadingService sensorReadingService;

    /**
     * Saves a new fault alert to the database.
     * Derives severity from ML confidence score and predicted class.
     * Returns mapped response DTO for immediate WebSocket broadcast.
     */
    public FaultAlertResponse saveAlert(SensorReading reading,
                                        MLPredictionResponse prediction,
                                        String recommendation,
                                        long latencyMs) {

        FaultAlert alert = FaultAlert.builder()
                .sensorReading(reading)
                .faultClass(FaultClass.valueOf(prediction.getPredictedClass()))
                .severityLevel(deriveSeverity(
                        prediction.getConfidence(),
                        prediction.getPredictedClass()))
                .confidence(prediction.getConfidence())
                .recommendation(recommendation)
                .latencyMs(latencyMs)
                .build();

        FaultAlert savedAlert = faultAlertRepository.save(alert);

        log.info("Alert saved for reading ID: {} — class: {} confidence: {}%",
                reading.getId(),
                prediction.getPredictedClass(),
                prediction.getConfidence() * 100);

        return faultAlertMapper.toResponse(savedAlert);
    }

    /**
     * Retrieves paginated fault alerts ordered by creation time descending.
     * Used by AlertController for dashboard recent alerts panel.
     */
    public Page<FaultAlertResponse> getRecentAlerts(int page, int size) {
        return faultAlertRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(faultAlertMapper::toResponse);
    }

    /**
     * Retrieves a single fault alert by its database ID.
     * Throws FaultAlertNotFoundException if ID does not exist.
     */
    public FaultAlertResponse getAlertById(Long id) {
        FaultAlert alert = faultAlertRepository.findById(id)
                .orElseThrow(() -> new FaultAlertNotFoundException(
                        "Fault alert with ID " + id + " not found"));

        return faultAlertMapper.toResponse(alert);
    }

    /**
     * Retrieves all alerts within a specified time range.
     * Used by AlertController for History page date filtering.
     */
    public List<FaultAlertResponse> getAlertsByDateRange(
            LocalDateTime from, LocalDateTime to) {

        List<FaultAlert> alerts = faultAlertRepository
                .findByCreatedAtBetween(from, to);

        return faultAlertMapper.toResponseList(alerts);
    }

    /**
     * Retrieves all alerts of a specific fault classification.
     * Used by AlertController and analytics for fault type filtering.
     */
    public List<FaultAlertResponse> getAlertsByFaultClass(FaultClass faultClass) {
        List<FaultAlert> alerts = faultAlertRepository
                .findByFaultClass(faultClass);

        return faultAlertMapper.toResponseList(alerts);
    }

    /**
     * Retrieves the single most recent fault alert in the system.
     * Used by StatusController to determine current pipeline status.
     * Returns Optional safely — empty if no alerts exist yet.
     */
    public Optional<FaultAlertResponse> getMostRecentAlert() {
        return faultAlertRepository
                .findTopByOrderByCreatedAtDesc()
                .map(faultAlertMapper::toResponse);
    }

    /**
     * Builds analytics summary for the History page.
     * Counts fault types, calculates pressure statistics across all three nodes.
     * Pressure min/max/average calculated across nodeA, nodeB, nodeC per reading.
     */
    public AnalyticsSummaryResponse getAnalyticsSummary(
            LocalDateTime from, LocalDateTime to) {

        List<SensorReading> readings = sensorReadingService
                .getReadingEntitiesByDateRange(from, to);

        // Fault counts
        long leakCount = faultAlertRepository.countByFaultClass(FaultClass.LEAK);
        long blockageCount = faultAlertRepository.countByFaultClass(FaultClass.BLOCKAGE);
        long totalReadings = readings.size();
        long normalCount = totalReadings - leakCount - blockageCount;

        // Average pressure across all three nodes per reading
        double averagePressure = readings.stream()
                .mapToDouble(r -> (r.getNodeAPressure()
                        + r.getNodeBPressure()
                        + r.getNodeCPressure()) / 3.0)
                .average()
                .orElse(0.0);

        // Min pressure across all three nodes
        double minPressure = readings.stream()
                .mapToDouble(r -> Math.min(r.getNodeAPressure(),
                        Math.min(r.getNodeBPressure(), r.getNodeCPressure())))
                .min()
                .orElse(0.0);

        // Max pressure across all three nodes
        double maxPressure = readings.stream()
                .mapToDouble(r -> Math.max(r.getNodeAPressure(),
                        Math.max(r.getNodeBPressure(), r.getNodeCPressure())))
                .max()
                .orElse(0.0);

        // Average flow velocity
        double averageFlowVelocity = readings.stream()
                .mapToDouble(SensorReading::getFlowVelocity)
                .average()
                .orElse(0.0);

        log.debug("Analytics summary built for range: {} to {}", from, to);

        return AnalyticsSummaryResponse.builder()
                .normalCount((int) normalCount)
                .leakCount((int) leakCount)
                .blockageCount((int) blockageCount)
                .totalReadings((int) totalReadings)
                .averagePressure(averagePressure)
                .minPressure(minPressure)
                .maxPressure(maxPressure)
                .averageFlowVelocity(averageFlowVelocity)
                .fromDate(from)
                .toDate(to)
                .build();
    }

    /**
     * Derives severity level from ML confidence score and predicted fault class.
     * NORMAL always returns NONE regardless of confidence.
     * Thresholds: below 0.50 = LOW, below 0.75 = MODERATE, above = CRITICAL.
     */
    private SeverityLevel deriveSeverity(double confidence, String predictedClass) {
        if ("NORMAL".equalsIgnoreCase(predictedClass)) {
            return SeverityLevel.NONE;
        }
        if (confidence < 0.50) {
            return SeverityLevel.LOW;
        }
        if (confidence < 0.75) {
            return SeverityLevel.MODERATE;
        }
        return SeverityLevel.CRITICAL;
    }
}