package io.github.darlene.leakdetectionapplication.service;

import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.ChatClient;


import lombok.RequiredArgsConstructor;
import lombok.extern.Slf4j.slf4j;

import io.github.darlene.leakdetectionapplication.dto.response.MLPredictionResponse;
import io.github.darlene.leakdetectionapplication.exception.RecommendationServiceException;
import io.github.darlene.leakdetectionapplication.domain.FaultClass;

import java.util.Map;

/**
 *  This service calls Ollama llm via spring AI to generate human readable maintenance recommendations based on the fault type, severity and all sensor readings.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService{

    private final ChatClient chatClient;

    public String generateRecommendation(MLPredictionResponse prediction, Map<String, Double> features){
         try{
             if ("NORMAL".equalsIgnoreCase(prediction.getPredictionClass())){
                 return "Pipeline operating normally. No action required";

                 String prompt = buildPrompt(prediction, features);

                 String recommendation = chatClient.prompt()
                         .user(prompt)
                         .call()
                         content();

                 log.debug("Recommendation generated for: {}", prediction.getPredictedClass());

                 return recommendation;

             } catch (Exception e){
                 log.error("LLM recommendation failed: {}", e.getMessage(), e);
                 throw new RecommendationServiceException("Failed to generate recommendation", e);
             }
         }
    }

    // Prrivate method to build prompt
    private String buildPrompt(MLPredictionResponse response, Map<String, Double> features){

        // Handling null values
        String predictedClass = prediction.getPredictedClass() !=null ? prediction.getPredictedClass() :"UNKNOWN";

        String severity = prediction.getLabel() !=null ? prediction.getLabel() :"UNSPECIFIED";

        double confidence = prediction.getConfidence != null ? prediction.getConfidence()*100 : 0.0;


        return String.format(
                """
                You are an expert pipeline engineer for a copper
                 tailings slurry pipeline system.
        
                 FAULT DETECTED: {prediction.getPredictedClass()}
                 CONFIDENCE: {prediction.getConfidence() * 100}%
                 SEVERITY: {prediction.getLabel()}
        
                 CURRENT SENSOR READINGS:
                 Node A Pressure: {features.get(node_a_pressure)} Pa
                 Node B Pressure: {features.get(node_b_pressure)} Pa
                 Node C Pressure: {features.get(node_c_pressure)} Pa
                 Flow Velocity:   {features.get(flow_velocity)} m/s
        
                 PRESSURE DIFFERENTIALS:
                 A to B drop: {features.get(pressure_drop_ab)} Pa
                 B to C drop: {features.get(pressure_drop_bc)} Pa
                 A to C drop: {features.get(pressure_drop_ac)} Pa
        
                 RATES OF PRESSURE CHANGE:
                 Node A: {features.get(dp_dt_a)} Pa/s
                 Node B: {features.get(dp_dt_b)} Pa/s
                 Node C: {features.get(dp_dt_c)} Pa/s
        
                 Provide a concise 4-5 sentence maintenance
                 recommendation for the pipeline operator.
                 
                 Make it read like human with no em dashes.
                 
                 Make it in point form.
                 """,

                prediction.getPredictedClass(),
                prediction.getConfidence() * 100,
                prediction.getLabel(),

                features.getOrDefault("node_a_pressure", 0.0),
                features.getOrDefault("node_b_pressure", 0.0),
                features.getOrDefault("node_c_pressure", 0.0),
                features.getOrDefault("flow_velocity", 0.0),

                features.getOrDefault("pressure_drop_ab", 0.0),
                features.getOrDefault("pressure_drop_bc", 0.0),
                features.getOrDefault("pressure_drop_ac", 0.0),

                features.getOrDefault("dp_dt_a", 0.0),
                features.getOrDefault("dp_dt_b", 0.0),
                features.getOrDefault("dp_dt_c", 0.0)

        );
    }
}