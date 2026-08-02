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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repository interface for SensorReading persistence operations.
 * Provides data access methods for pipeline sensor telemetry data.
 * Extends JpaRepository to inherit standard CRUD operations.
 * Custom methods support dashboard chart population and analytics.
 */
@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    /**
     * Retrieves all sensor readings ordered by readingTime descending.
     * Used to populate the live pressure chart on the dashboard.
     */
    Page<SensorReading> findAllByOrderByReadingTimeDesc(Pageable pageable);

    /**
     * Retrieves sensor readings recorded within a specified time range.
     * Used by the History page to filter readings by date.
     */
    List<SensorReading> findByReadingTimeBetween(OffsetDateTime from, OffsetDateTime to);

    /**
     * Retrieves all readings transmitted by a specific ESP32 device.
     * Used for device-level filtering and diagnostics.
     */
    List<SensorReading> findByDeviceId(String deviceId);

    List<SensorReading> findTop100ByScenarioContainingIgnoreCaseOrderByReadingTimeAsc(String scenario);
}