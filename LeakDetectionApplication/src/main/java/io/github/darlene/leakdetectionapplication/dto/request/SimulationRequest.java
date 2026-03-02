package io.github.darlene.leakdetection.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import io.github.darlene.leakdetection.domain.FaultClass;
import io.github.darlene.leakdetection.domain.SeverityLevel;

/**
 * Request DTO for manual fault injection via the simulation control panel
 * Received Via POST api/simulate/inject-fault
 * Used during HIL validation to demonstrate end to end response.
 */

@Data
@AllArgsConstructor
@NoAragsConstructor
public class SimulationRequest {

    @JsonProperty("scenario_name")
    @NotBlank(message = "Scenario name cannot be empty")
    private String scenarioName;

    @NotNull(message = "Fault class required.")
    private FaultClass faultClass;

    @NotNull(message = "Severity level required.")
    private SeverityLevel severityLevel;

    @NotNull(message = "Duration Seconds cannot be empty")
    @Min(value = 5, message = "Simulation must run for atleast 5 seconds")
    @Max(value = 300, message = "Simulation must run for at most 300 seconds")
    private Integer durationSeconds;
}