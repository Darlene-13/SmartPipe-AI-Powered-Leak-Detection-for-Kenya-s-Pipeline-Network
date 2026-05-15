@Data
public class RecommendationRequest {
    @NotBlank
    private String pipelineSegment;   // e.g. "A-B", "B-C"

    @NotEmpty
    private Map<String, Double> features;   // keys match FeatureExtractionService output
}