package io.github.darlene.leakdetectionapplication.services;

import org.springframework.stereotype;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import lombok.RequiredArgsConstruction;
import lombok.extern.slf4j.Slf4j;

import io.github.darlene.leakdetectionapplication.repository.FaultAlertRepository;
import io.github.darlene.leakdetectionapplication.repository.SensorReadingRepository;
import io.github.darlene.leakdetectionapplication.domain.FaultAlert;
import io.github.darlene.leakdetectionapplication.domain.SensorReading;
import io.github.darlene.leakdetectionapplication.mapper.FaultAlertMapper;
import io.github.darlene.leakdetectionapplication.dto.response.FaultAlertResponse;
import io.github.darlene.leakdetectionapplication.dto.response.AnalyticsSummaryResponse;
import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;

import io.github.darlene.leakdetectionapplication.exception.FaultAlertNotFoundException;

import java.time.LocalDateTime
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final FaultAlertRepository faultAlertRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final FaultAlertMapper faultAlertMapper;
    private final LatencyTrackingService latencyTrackingService;

    public FaultAlertReponse saveAlert(SensorReading reading,
                          MLPredictionResponse prediction,
                          String recommendation,
                          long latencyMs){
        FaultAlert alert = FaultAlert.builder()
                .sensorReading(reading)
                .faultClass(FaultClass.valueOf(prediction.getPredictedClass()))
                .severityLevel(deriveSeverity(prediction.getConfidence(), prediction.getPredictedClass()))
                .confidence(prediction.getConfidence())
                .recommendation(recommendation)
                .latencyMs(latencyMs)
                .build();

        FaultAlert savedAlert = FaultAlertRepository.save(alert);

        log.info("Alert saved for reading: {}");

        return faultAlertMapper.toReponse(savedAlert);
    }

    public Page<FaultAlertReponse> getRecentAlerts(int page, int size){
        return faultAlertRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(faultAlertMapper::toResponse);
    }


    public FaultAlertReponse getAlertById(Long id){
        FaultAlert alert = faultAlertRepository.findById(id)
                .orElseThrow(() -> new FaultAlertNotFoundException("Alert not found: {}"));

        return faultAlertMapper.toResponse(alert);
    }

    public List<FaultAlertResponse> getAlertsByDateRange(LocalDateTime from, LocalDateTime to){

        List<FaultAlert> alerts = faultAlertRepository.findByCreatedAtBetween(from, to);
        return faultAlertMapper.toResponse(alerts);
    }

    public List<FaultAlert> getAlertsByFaultClass(FaultClass faultClass){

        List<FaultAlert> alert = faultAlertRepository.findByFaultClass(faultClass);
        return faultAlertMapper.toResponse(alerts);
    }
    public Optional<FaultAlertResponse> getMostRecentAlert(){
        return faultAlertRepository.findTopByOrderByCreatedDesc()
                .map(faultAlertMapper::toReponse);
    }

    public AnalyticsSummaryReponse getAnalyticsSummary(LocalDateTime from, LocalDateTime to){
        // Count normal leak reading from sensor reading repo
        long normalCount = sensorReadingRepository.countByStatus(FaultClass.NORMAL);
        long leakCount = faultAlertRepository.countByFaultClass(FaultClass.LEAK);
        long blockageCount = faultAlertRepository.countByClass(FaultClass.BLOCKAGE);

        long totalCount = normalCount + leakCount + blockageCount;

        List<SensorReading> readingsInRange = sensorReadingRepository.findByReadingTimeBetween(from, to);

        double avgPressure = readingsInRange.stream().mapToDouble(SensorReading::getPressure).average().orElse(0.0);
        double minPressure = readingsInRange.stream().mapToDouble(SensorReading::getPressure).min().orElse(0.0);
        double maxPressure = readingsInRange.stream().mapToDouble(SensorReading::getPressure).max().orElse(0.0);
        double avgFlowVelocity = readingsInRange.stream().mapToDouble(SensorReading::getFlowVelocity).average().orElse(0.0);



    }

    private deriveSeverity(double confidence, String predictedClass){
        if()
    }

}