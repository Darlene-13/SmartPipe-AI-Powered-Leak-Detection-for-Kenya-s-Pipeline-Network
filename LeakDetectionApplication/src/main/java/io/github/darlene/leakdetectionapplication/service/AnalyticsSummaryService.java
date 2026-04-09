package io.github.darlene.leakdetectionapplication.service;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.github.darlene.leakdetectionapplication.dto.response.AnalyticsSummaryResponse;
import io.github.darlene.leakdetectionapplication.dto.response.LatencyStatsResponse;
import io.github.darlene.leakdetectionapplication.repository.FaultAlertRepository;

import io.github.darlene.leakdetectionapplication.domain.FaultClass;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;



@Service
@RequiredArgsConstructor
@Slf4j


public class AnalyticsSummaryService{

    private final AlertService alertService;
    private final SensorReadingService sensorReadingService;
    private final LatencyTrackingService latencyTrackingService;
    private final FaultAlertRepository faultAlertRepository;


    public AnalyticsSummaryResponse getSummary(LocalDateTime from, LocalDateTime to){
        log.info("Build analytics summary");
        return alertService.getAnalyticsSummary(from, to);
    }

    public LatencyStatsResponse getLatencyStats(){
        log.info("Fetching latency Statistics");
        return latencyTrackingService.getLatencyStatsResponse();
    }

    public Map<String, Long> getFaultDistribution(LocalDateTime from, LocalDateTime to) {
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
        long normalCount = totalReadings - leakCount - blockageCount;

        Map<String, Long> distribution = new HashMap<>();
        distribution.put("NORMAL", normalCount);
        distribution.put("BLOCKAGE", blockageCount);
        distribution.put("LEAK", leakCount);

        return distribution;
    }
}
