package io.github.darlene.leakdetectionapplication.service;


/**
 *This file is called by the MQTT subscriver for every incoming sensor reading.
 * It co-ordinates with other sensor services in the correct sequence.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessingService{
    private final FeatureExtractionService featureExtractionService;
    private final MLBridgeService mlBridgeService;
    private final SensorReadingRepository sensorReadingRepository;
    private final AlertService alertService;
    private final RecommendationService recommendationService;
    private final LatencyTrackingService latencyTrackingService;
    private final MqttPublisher mqttPublisher;
    private final AlertWebSocketHandler alertWebSocketHandler;

    // Process reading
    public void processReading(SensorReadingRequest request){
         String readingId = UUID.randomUUID().toString();
         latencyTrackingService.startTracking(readingId);
         log.debug("Processing reading from device: {}", request.getDeviceId());

         try{
             // Save the raw reading
             SensorReading entity = convertToEntity(request);
             Map<String, Double> features = featureExtractionService.extractFeatures(request);
             entity.setNodeDpDtA(features.get("node_dp_dt_a"));
             entity.setNodeDpDtB(features.get("node_dp_dt_b"));
             entity.setNodeDpDtC(features.get("node_dp_dt_c"));
             SensorReading savedReading = sensorReadingRepository.save(entity);

             // ML Prediction
             String prediction = mlBridgeService.predict(request);
             log.debug("Prediction: {} confidence: {}%", request.getDeviceId());

             // Handling fault or normal
             if (!"NORMAL".equalsIgnoreCase(prediction.getPredictedClass())){
                 // Generate recommendation
                 String recommendation = recommendationService.generateRecommendation(prediction, features);
                 Double latencyMs = latencyTrackingService.recordLatency(readingId);

                 // Save alert
                 FaultAlert savedAlert = alertService.saveAlert(savedReading, prediction, recommendation, latencyMs);

                 // Publish LED status
                 String ledClolor = resolveLedColor(prediction.getLabel());
                 mqttPublisher.publishedLedStatus(ledColor);

                 // Broadcast to dashboard
                 alertWebSocketHandler.broadcastAlert(alertResponse);

                 log.info("Fault processed: {} in {}ms",alertResponse);
             } else {
                 latencyTrackingService.recordLatency(readingId);
                 mqttPublisher.publishLedStatus("GREEN");
                 log.debug("Normal reading processed", request.getDeviceId())
             }
         } catch (MLServiceUnavailableException e){
             log.error("ML Service unavailable for reading: {}", readingId, e);
             latencyTrackingService.recordLatency(readingId);
             mqttPublisher.publishLedStatus("BLUE");
             throw e;
         } catch (Exception e){
             log.error("Processing failed for readings: {}", readingId);
             latencyTrackingService.recordLatency(readingId);
             throw e;
         }

    }

    // Method to simulate scenario
    public FaultALertResponse simulateScenario(String scenarioName){
        valid scenarios = ["NORMAL_BASELINE", "LEAK_INCIPIENT", "LEAK_MODERATE", "LEAK_CRITICAL", "BLOCKAGE_25", "BLOCKAGE_50", "BLOCKAGE_75"}


    }

    // Private method to convertEntity
    private SensorReading convertToEntity(SensorReadingRequest request){
        return SensorReading.builder()
                .deviceId(request.getDeviceId(request))
                .readingTime(request.getDeviceId(request))
                .nodeAPressure(request.getNodeAPressure())
                .nodeBPressure(request.getNodeBPressure())
                .nodeCPressure(request.getNodeCPressure())
                .nodeFlowVelocity(request.getFlowVelocity())
                .nodeDpDtA(request.DpDtA())
                .nodeDpDtB(request.DpDtB())
                .nodeDpDtc(request.DpDtC())
                .build();
    }

    // Private method for resolved
    private String resolvedColor(String severityLevel){
        return switch (severityLabel){
            case "CRITICAL" -> "RED";
            case "MODERATE" -> "YELLOW";
            case "LOW" -> "YELLOW";
            default -> "GREEN";
        };
    }

}