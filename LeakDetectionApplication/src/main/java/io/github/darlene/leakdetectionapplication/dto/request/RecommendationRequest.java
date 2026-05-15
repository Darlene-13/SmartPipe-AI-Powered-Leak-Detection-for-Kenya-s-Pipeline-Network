package io.github.darlene.leakdetectionapplication.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

@Data
public class RecommendationRequest {

    @NotBlank
    private String pipelineSegment;

    @NotEmpty
    private Map<String, Double> features;   // keys match FeatureExtractionService output
}