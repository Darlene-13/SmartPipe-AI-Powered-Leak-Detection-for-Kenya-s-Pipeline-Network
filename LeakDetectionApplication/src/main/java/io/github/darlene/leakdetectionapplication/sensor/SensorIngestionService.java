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

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.darlene.leakdetectionapplication.sensor.SensorReading;
import io.github.darlene.leakdetectionapplication.pipeline.ProcessingService;
import io.github.darlene.leakdetectionapplication.sensor.SensorReadingMapper;
import io.github.darlene.leakdetectionapplication.sensor.SensorReadingRequest;

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