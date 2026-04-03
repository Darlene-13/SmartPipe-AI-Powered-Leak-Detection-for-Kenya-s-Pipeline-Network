package io.gihub.darlene.leakdetectionapplication.service;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

// Lombok
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import io.github.darlene.leakdetectionapplication.repository.SensorReadingRepository;
import io.github.darlene.leakdetectionapplication.domain.SensorReading;
import io.github.darlene.leakdetectionapplication.dto.response.SensorReadingResponse;
import io.github.darlene.leakdetectionapplication.exception.SensorReadingNotFoundException;
import io.github.darlene.leakdetectionapplication.mapper.SensorReadingMapper;

import io.github.darlene.leakdetectionapplication.domain.FaultClass;

// Java imports
import java.time.LocalDateTime;
import java.util.List;
/**
 * Handles all sensor reeadings queries for the dashboard rest api.
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class SensorReadingService{

    private final SensorReadingRepository sensorReadingRepository;
    private final SensorReadingMapper sensorReadingMapper;

    public Page<SensorReadingResponse> getLatestReading(int page, int size){
        log.info("Fetching latest sensor readings - page: {}, size:{}", page, size);

        return sensorReadingRepository.
                findAllByOrderByReadingTimeDesc(PageRequest.of(page, size))
                .map(SensorReadingMapper::toResponse);

    }

    // Get reading by Id
    public SensorReadingResponse getReadingById(Long Id){
        log.info("Fetching sensor reading");
        SensorReading reading = sensorReadingRepository.findById(id)
                .orElseThrow(() -> new SensorReadingNotFoundException(
                        "Sensor reading with ID "+ id + " not found"
                ));
        return sensorReadingMapper.toReponse(reading);

    }

    // Get sensor readings by data
    public List<SensorReadingResponse> getReadingsByDateRange(LocalDateTime from, LocalDateTime to){

        List<SensorReading> readings = sensorReadingRepository.findByReadingTimeBetween((from, to);
        return sensorReadingMapper.toResponseList(readings);
    }

    //Get readings by device
    public List<SensorReadingResponse> getReadingsByDeviceId(String deviceId){

        List<SensorReading> readings = sensorReadingRepository.findByDeviceId(String deviceId);

        return SensorReadingMapper.toResponseList(readings);
    }

    // Get total readings
    public long getTotalReadingCount(){
        return sensorReadingRepository.count();
    }
}