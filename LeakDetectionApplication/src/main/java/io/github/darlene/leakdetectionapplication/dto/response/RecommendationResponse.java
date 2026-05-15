@Data
@Builder
public class RecommendationResponse {
    private String pipelineSegment;
    private String predictedClass;
    private Double confidence;
    private String label;
    private String recommendation;
    private OffsetDateTime generatedAt;
}