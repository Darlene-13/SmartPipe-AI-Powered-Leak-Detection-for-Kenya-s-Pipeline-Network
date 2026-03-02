package io.github.darlene.leakdetection.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO representing a machine learning prediction result
 * Returned by POST /api/predictions
 * Used to provide the predicted class, label, confidence, and prediction timestamp to the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLPredictionResponse {

    /** The predicted class returned by the ML model. */
    private String predictedClass;

    /** Human-readable label corresponding to the predicted class. */
    private String label;

    /** Confidence score of the prediction (0.0 - 1.0). */
    private double confidence;

    /** Timestamp when the prediction was made. */
    private LocalDateTime predictionTime;
}