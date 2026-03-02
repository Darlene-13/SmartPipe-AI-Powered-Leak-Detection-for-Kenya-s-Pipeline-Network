package io.github.darlene.leakdetection.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * Response DTO representing the current operational status of the pipeline system
 *  Returned by GET /api/status/current
 *  Also broadcast via websocket on every status change.
 */


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class SystemStatusResponse {

    private String status;

    private String description;

    private String colorCode;

    private boolean requiresAction;

    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;

    private Integer activeAlerts;
}