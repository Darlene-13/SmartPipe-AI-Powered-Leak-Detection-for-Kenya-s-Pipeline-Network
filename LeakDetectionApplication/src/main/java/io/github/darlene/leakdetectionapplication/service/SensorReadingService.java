package io.github.darlene.leakdetectionapplication.service;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import io.github.darlene.leakdetectionapplication.repository.SensorReadingRepository;
import io.github.darlene.leakdetectionapplication.domain.SensorReading;
import io.github.darlene.leakdetectionapplication.dto.response.SensorReadingResponse;
import io.github.darlene.leakdetectionapplication.exception.SensorReadingNotFoundException;
import io.github.darlene.leakdetectionapplication.mapper.SensorReadingMapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service handling all sensor reading queries for the dashboard REST API.
 * Provides pagination, date range filtering, and device-level filtering.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensorReadingService {

    private final SensorReadingRepository sensorReadingRepository;
    private final SensorReadingMapper sensorReadingMapper;

    /**
     * Retrieves paginated sensor readings ordered by timestamp descending.
     * Used by SensorController for dashboard live pressure chart.
     */
    public Page<SensorReadingResponse> getLatestReadings(int page, int size) {
        log.info("Fetching latest sensor readings - page: {}, size: {}", page, size);
        return sensorReadingRepository
                .findAllByOrderByReadingTimeDesc(PageRequest.of(page, size))
                .map(sensorReadingMapper::toResponse);
    }

    /**
     * Retrieves a single sensor reading by its database ID.
     * Throws SensorReadingNotFoundException if ID does not exist.
     */
    public SensorReadingResponse getReadingById(Long id) {
        log.info("Fetching sensor reading with ID: {}", id);
        SensorReading reading = sensorReadingRepository.findById(id)
                .orElseThrow(() -> new SensorReadingNotFoundException(
                        "Sensor reading with ID " + id + " not found"));
        return sensorReadingMapper.toResponse(reading);
    }

    /**
     * Retrieves all sensor readings within a specified time range.
     * Used by SensorController for History page date filtering.
     */
    public List<SensorReadingResponse> getReadingsByDateRange(
            LocalDateTime from, LocalDateTime to) {
        log.info("Fetching readings between {} and {}", from, to);
        List<SensorReading> readings = sensorReadingRepository
                .findByReadingTimeBetween(from, to);
        return sensorReadingMapper.toResponseList(readings);
    }

    /**
     * Returns raw domain entities for internal service calculations.
     * Used by AlertService for pressure statistics in analytics summary.
     */
    public List<SensorReading> getReadingEntitiesByDateRange(
            LocalDateTime from, LocalDateTime to) {
        return sensorReadingRepository.findByReadingTimeBetween(from, to);
    }

    /**
     * Retrieves all readings from a specific ESP32 device.
     * Used by SensorController for device-level filtering.
     */
    public List<SensorReadingResponse> getReadingsByDeviceId(String deviceId) {
        log.info("Fetching readings for device: {}", deviceId);
        List<SensorReading> readings = sensorReadingRepository
                .findByDeviceId(deviceId);
        if (readings.isEmpty()) {
            log.warn("No readings found for device: {}", deviceId);
        }
        return sensorReadingMapper.toResponseList(readings);
    }

    /**
     * Returns total count of all sensor readings in the database.
     * Used by AnalyticsController for summary statistics.
     */
    public long getTotalReadingCount() {
        return sensorReadingRepository.count();
    }
}