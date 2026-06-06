package io.github.darlene.leakdetectionapplication.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.darlene.leakdetectionapplication.domain.SensorReading;
import io.github.darlene.leakdetectionapplication.service.ProcessingService;
import io.github.darlene.leakdetectionapplication.mapper.SensorReadingMapper;
import io.github.darlene.leakdetectionapplication.dto.request.SensorReadingRequest;

@Service
@Slf4j
public class SensorIngestionService {

    private final ObjectMapper objectMapper;
    private final SensorReadingMapper mapper;
    private final ProcessingService processingService;

    public SensorIngestionService(
            ObjectMapper objectMapper,
            SensorReadingMapper mapper,
            ProcessingService processingService) {

        this.objectMapper = objectMapper;
        this.mapper = mapper;
        this.processingService = processingService;
    }

    public void handle(String payload) {
        try {
            SensorReadingRequest request =
                    objectMapper.readValue(payload, SensorReadingRequest.class);
            SensorReading entity = mapper.toEntity(request);
            processingService.processReading(entity);
        } catch (Exception e) {
            log.error("Failed to process sensor payload: {}", payload, e);
        }
    }
}