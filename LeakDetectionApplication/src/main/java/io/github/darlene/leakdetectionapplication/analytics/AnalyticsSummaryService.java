package io.github.darlene.leakdetectionapplication.analytics;

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

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.darlene.leakdetectionapplication.analytics.AnalyticsSummaryResponse;
import io.github.darlene.leakdetectionapplication.analytics.LatencyStatsResponse;
import io.github.darlene.leakdetectionapplication.alert.FaultAlertRepository;
import io.github.darlene.leakdetectionapplication.alert.FaultClass;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsSummaryService {

    private final AlertService alertService;
    private final SensorReadingService sensorReadingService;
    private final LatencyTrackingService latencyTrackingService;
    private final FaultAlertRepository faultAlertRepository;

    public AnalyticsSummaryResponse getSummary(OffsetDateTime from, OffsetDateTime to) {
        log.info("Building analytics summary from {} to {}", from, to);
        return alertService.getAnalyticsSummary(from, to);
    }

    public LatencyStatsResponse getLatencyStats() {
        log.info("Fetching latency statistics");
        return latencyTrackingService.getLatencyStatsResponse();
    }

    public Map<String, Long> getFaultDistribution(OffsetDateTime from, OffsetDateTime to) {
        log.info("Building fault distribution from {} to {}", from, to);

        long leakCount = faultAlertRepository
                .findByFaultClassAndCreatedAtBetween(FaultClass.LEAK, from, to)
                .size();

        long blockageCount = faultAlertRepository
                .findByFaultClassAndCreatedAtBetween(FaultClass.BLOCKAGE, from, to)
                .size();

        long totalReadings = sensorReadingService
                .getReadingsByDateRange(from, to)
                .size();

        long normalCount = Math.max(0, totalReadings - leakCount - blockageCount);

        Map<String, Long> distribution = new HashMap<>();
        distribution.put("NORMAL", normalCount);
        distribution.put("BLOCKAGE", blockageCount);
        distribution.put("LEAK", leakCount);
        return distribution;
    }
}