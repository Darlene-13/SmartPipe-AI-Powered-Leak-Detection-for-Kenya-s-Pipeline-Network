package io.github.darlene.leakdetectionapplication.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO representing a machine learning prediction result.
 *
 * Maps exactly to Flask /predict response:
 * {
 *   "predicted_class": 2,
 *   "label": "Blockage",
 *   "confidence": 0.9983,
 *   "probabilities": { "Normal": 0.0014, "Leak": 0.0003, "Blockage": 0.9983 },
 *   "status": "predicted",
 *   "window_progress": "10/10",
 *   "device_id": "ESP32_NODE_01"
 * }
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLPredictionResponse {

    @JsonProperty("predicted_class")
    private Integer predictedClassInt;

    @JsonProperty("label")
    private String label;

    @JsonProperty("confidence")
    private Double confidence;

    @JsonProperty("probabilities")
    private Map<String, Double> probabilities;

    @JsonProperty("status")
    private String status;

    @JsonProperty("window_progress")
    private String windowProgress;

    @JsonProperty("device_id")
    private String deviceId;

    public String getPredictedClass() {
        if (label == null) return "NORMAL";
        return switch (label.toUpperCase()) {
            case "LEAK"     -> "LEAK";
            case "BLOCKAGE" -> "BLOCKAGE";
            default         -> "NORMAL";
        };
    }

    public boolean isCollecting() {
        return "collecting".equalsIgnoreCase(status);
    }

    public boolean isPredicted() {
        return "predicted".equalsIgnoreCase(status);
    }
}