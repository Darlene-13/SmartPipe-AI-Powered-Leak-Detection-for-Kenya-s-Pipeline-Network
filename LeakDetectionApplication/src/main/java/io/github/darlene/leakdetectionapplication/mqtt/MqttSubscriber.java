package io.github.darlene.leakdetectionapplication.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.github.darlene.leakdetectionapplication.dto.request.SensorReadingRequest;
import io.github.darlene.leakdetectionapplication.service.ProcessingService;

import java.util.UUID;

/**
 * MQTT subscriber configuration.
 * Listens on configured topics and routes messages to ProcessingService.
 * Uses ExecutorChannel with thread pool for concurrent message processing.
 */
@Slf4j
@Configuration
public class MqttSubscriber {

    private final MqttPahoClientFactory mqttClientFactory;
    private final ProcessingService processingService;
    private final ObjectMapper objectMapper;

    @Value("${mqtt.topics.sensor-data}")
    private String sensorDataTopic;

    @Value("${mqtt.subscribe.topics}")
    private String[] subscribeTopics;

    public MqttSubscriber(
            MqttPahoClientFactory mqttClientFactory,
            ProcessingService processingService,
            ObjectMapper objectMapper) {
        this.mqttClientFactory = mqttClientFactory;
        this.processingService = processingService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public ThreadPoolTaskExecutor mqttExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("mqtt-proc-");
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        return executor;
    }

    @Bean
    public MessageChannel mqttInputChannel(
            ThreadPoolTaskExecutor mqttExecutor) {

        ExecutorChannel channel = new ExecutorChannel(mqttExecutor);

        channel.addInterceptor(new ChannelInterceptor() {
            @Override
            public org.springframework.messaging.Message<?> preSend(
                    org.springframework.messaging.Message<?> message,
                    MessageChannel ch) {

                String topic = (String) message.getHeaders()
                        .get("mqtt_receivedTopic");
                String payload = message.getPayload().toString();

                if (topic == null) {
                    log.warn("Null topic received — ignoring message");
                    return message;
                }

                log.debug("MQTT message received — topic: {} payload: {}",
                        topic, payload);

                // Route sensor data to ProcessingService
                if (topic.matches("pipeline/sensors/.+")) {
                    handleSensorReading(payload);

                } else {
                    log.warn("Unknown MQTT topic: {}", topic);
                }

                return message;
            }
        });

        return channel;
    }

    @Bean
    public MqttPahoMessageDrivenChannelAdapter inboundAdapter(
            @Qualifier("mqttInputChannel") MessageChannel mqttInputChannel) {

        String subscriberClientId = "subscriber-" + UUID.randomUUID();

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        subscriberClientId,
                        mqttClientFactory,
                        subscribeTopics
                );

        adapter.setOutputChannel(mqttInputChannel);
        return adapter;
    }

    /**
     * Deserializes incoming JSON payload and routes to ProcessingService.
     * Logs error and drops message if deserialization fails.
     */
    private void handleSensorReading(String payload) {
        try {
            SensorReadingRequest request = objectMapper
                    .readValue(payload, SensorReadingRequest.class);

            log.info("Sensor reading received from device: {}",
                    request.getDeviceId());

            processingService.processReading(request);

        } catch (Exception e) {
            log.error("Failed to process sensor reading payload: {} error: {}",
                    payload, e.getMessage());
        }
    }
}