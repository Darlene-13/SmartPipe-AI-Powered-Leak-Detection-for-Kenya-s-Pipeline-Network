package io.github.darlene.leakdetectionapplication.repository;

import io.github.darlene.leakdetectionapplication.domain.FaultAlert;
import io.github.darlene.leakdetectionapplication.domain.FaultClass;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * Repository interface for FaultAlert persistence operations.
 * Provides data access methods for detected pipeline fault events.
 * Extends JpaRepository to inherit standard CRUD operations.
 * Custom methods support dashboard alerts, analytics, and system status.
 */

@Repository
public interface FaultAlertRepository extends JpaRepository<FaultAlert, Long>{

    /**
     * Retrieves all fault alerts ordered by creation time descending.
     * Used to populate the recent alerts panel on the dashboard.
     */
    Page<FaultAlert> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Retrieves fault alerts recorded within a specified time range.
     * Used by the History page to filter alerts by date.
     */
    List<FaultAlert> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    /**
     * Retrieves all alerts of a specific fault classification.
     * Used by analytics to separate leak and blockage event histories.
     */
    List<FaultAlert> findByFaultClass(FaultClass faultClass);

    /**
     * Counts the total number of alerts for a specific fault class.
     * Used to generate fault distribution statistics for History page charts.
     */
    long countByFaultClass(FaultClass faultClass);

    /**
     * Retrieves the single most recent fault alert in the system.
     * Used by StatusController to determine current pipeline system status.
     * Returns Optional to safely handle the case where no alerts exist yet.
     */
    Optional<FaultAlert> findTopByOrderByCreatedAtDesc();

    /**
     * Retrieves all alerts of a specific fault class within a time range.
     * Used by analytics summary to count leaks and blockages per date range.
     */

    List<FaultAlert>  findByFaultClassAndCreatedAtBetween(
            FaultClass faultClass,
            LocalDateTime from,
            LocalDateTime to
    );
}