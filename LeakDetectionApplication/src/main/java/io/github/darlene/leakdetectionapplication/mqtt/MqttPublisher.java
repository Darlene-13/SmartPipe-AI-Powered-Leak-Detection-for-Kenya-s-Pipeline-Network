package io.github.darlene.leakdetectionapplication.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import java.util.UUID;

@Slf4j
@Configuration
public class MqttPublisher {

    private final MqttPahoClientFactory mqttClientFactory;

    @Value("${mqtt.publish.topic.config}")
    private String configTopicTemplate;

    @Value("${mqtt.publish.topic.alerts}")
    private String alertTopicTemplate;

    @Value("${mqtt.publish.topic.commands}")
    private String commandTopicTemplate;

    @Value("${mqtt.topics.led-status}")
    private String ledStatusTopic;

    public MqttPublisher(MqttPahoClientFactory mqttClientFactory) {
        this.mqttClientFactory = mqttClientFactory;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public MqttPahoMessageHandler outboundAdapter() {
        String publisherClientId = "publisher-" + UUID.randomUUID();
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(
                publisherClientId,
                mqttClientFactory
        );
        handler.setAsync(true);
        handler.setDefaultTopic("pipeline/alerts/default");
        return handler;
    }

    @Bean
    public MessagingTemplate mqttTemplate() {
        return new MessagingTemplate(mqttOutboundChannel());
    }

    public void publishConfig(String deviceId, String jsonPayload) {
        String topic = String.format(configTopicTemplate, deviceId);
        sendMessage(topic, jsonPayload);
        log.info("Config published to: {}", topic);
    }

    public void publishAlert(String deviceId, String jsonPayload) {
        String topic = String.format(alertTopicTemplate, deviceId);
        sendMessage(topic, jsonPayload);
        log.info("Alert published to: {}", topic);
    }

    public void publishCommand(String deviceId, String jsonPayload) {
        String topic = String.format(commandTopicTemplate, deviceId);
        sendMessage(topic, jsonPayload);
        log.info("Command published to: {}", topic);
    }

    private void sendMessage(String topic, String payload) {
        mqttTemplate().send(
                mqttOutboundChannel(),
                MessageBuilder.withPayload(payload)
                        .setHeader("mqtt_topic", topic)
                        .build()
        );
    }

    public void publishLedStatus(String ledColor){
        String payload = String.format("{\"color\":\"%s\"}", ledColor);
        sendMessage(ledStatusTopic, payload);
        log.info("LED status published: {} to topic: {}", ledColor, ledStatusTopic);
    }
}