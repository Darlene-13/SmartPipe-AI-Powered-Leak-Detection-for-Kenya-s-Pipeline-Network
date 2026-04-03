package io.github.darlene.leakdetectionapplication.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 *  Response DTO representing a single sensor reading
 *  Returned by GET /api/sensors/readings/latest
 *  and GET /api/sensors/readings/history
 *  Used to populate dashboard pressure in charts.
 */

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor


public class SensorReadingResponse{

    private Long id;

    private String deviceId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readingTime;

    private Double nodeAPressure;

    private Double nodeBPressure;

    private Double nodeCPressure;

    private Double flowVelocity;

    private Double dpDtA;

    private Double dpDtB;

    private Double dpDtC;

    private String scenario;

    private String prediction;

}