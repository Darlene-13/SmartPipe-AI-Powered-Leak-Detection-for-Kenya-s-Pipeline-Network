package io.github.darlene.leakdetectionapplication.service;

import org.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.github.darlene.leakdetectionapplication.dto.response.AnalyticsSummaryResponse;
import io.github.darlene.leakdetectionapplication.dto.response.LatencyStatsResponse;

import io.github.darlene.leakdetectionapplication.domain.FaultClass;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;



@Service
@RequiredArgsConstructor
@Slf4j


public class AnalyticsSummary{

    private final AlertService alertService;
    private final SensorReadingService sensorReadingService;
    private final LatencyTrackingService;

    public AnalyticsSummaryResponse getSummary(LocalDateTime from, LocalDateTime to){
        log.info("Build analytics summary");
        return alertService.getAnalyticsSummary(from, to);
    }

    public LatencyStatsResponse getLatencyStats(){
        log.info("Fetching latency Statistics");
        return getLatencyStatsResponse();
    }

    public Map<String, Long> getFaultDistribution(){
        log.info("Building fault Distribution");
        long leakCount = alertService.getAlertsByFaultClass(FaultClass.LEAK).size();
        long blockageCount = alertService.getAlertsByFaultClass(FaultClass.BLOCKAGE).size();
        long totalReadings = sensorReadingService.getTotalReadingCount();
        long normalCount = totalReadings - leakCount - blockageCount;

        Map<String, Long> distribution = new HashMap<>();
        distribution.put("NORMAL", normalCount);
        distribitution.put("BLOCKAGE", blockageCount);
        distribution.put("LEAK", leakCount);

        return distribution;

    }
}