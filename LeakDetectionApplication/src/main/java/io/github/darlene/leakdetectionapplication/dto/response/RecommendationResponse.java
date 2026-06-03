package io.github.darlene.leakdetectionapplication.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class RecommendationResponse {
    private String deviceId;
    private String predictedClass;
    private Double confidence;
    private String label;
    private String recommendation;
    private OffsetDateTime generatedAt;
}