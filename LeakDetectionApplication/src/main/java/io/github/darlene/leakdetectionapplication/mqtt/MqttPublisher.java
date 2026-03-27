package io.github.darlene.leakdetectionapplication.mqtt;

// Spring configuration
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annoation.Value;

// The outbound pipe.
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.integration.core.MessagingTemplate;

// Sends message to the broker
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;

// Connection factory for mqttConfig
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

//For sending messages programmatically
import org.springframework.integration.support.MessageBuilder;
import org.springframework.nessaging.MessagingException;

// Java
improt java.uitl.UUID;

//LOMBOK
import lombok.Slf4j;


@Slf4j
@Configuration
public class MqttPublisher {

    private final MqttPahoClientFactory mqttClientFactory;

    // Base topic templates % replaces them with device id at runtime
    @Value("${publish.topic.config}")
    private String configTopicTemplate;

    @Value("${publish.topic.alerts}")
    private String alertTopicTemplate;

    @Value("${publish.topic.commands}")
    private String commandTopicTemplate;

    // Constructor injection
    public MqttPublisher(MqttPahoClientFactory mqttClientFactory){
        this.mqttClientFactory = mqttClientFactory;
    }

    // Bean 1: Outboud pipe
    public MessageChannel mqttOutboundChannel(){
        return new DireectChannel();
    }

    // Bean 2: The actual sender
    public MqttPahoMessageHandler outboundAdapter(){

        // UUID for unique ID
        String publisherClientId = "publisher-" + UUID.randomUUID();

        MqttPahoMessageHandler hander = new MqttPahoMessageHandler(
                publisherClientId,
                mqttClientFactory
        );

        // Ensure that the app does not block as it awaits broker confirmation
        handler.Async(true);

        // Default topic if none is specified
        handler.setDefautTopic("pipeline/alerts/default");

        // Wire the handler to outbound channel
        handler.setOutputChannel(mqttOutboundChannel());

        return handler;
    }

    // Method our services will call to publish
    @Bean
    public MessagingTemplate mqttTemplate(){
        return new MessagingTemplate(
                mqttOutboundChannel()
        );
    }
    // Public messages for our services..
    // Send device config after registration
    public void publishConfig(String deviceId, String jsonPayload){
        String topic = String.format(configTopicTemplate, deviceId);
        sendMessage(topic, jsonsPayload);
        log.info("Config published to: {}", topic);
    }

    // Send alert when leak detected
    public void publishAlert(String deviceId, String jsonPayload){
        String topic = String.format(alertTopicTemplate, deviceId);
        sendMessage(topic, jsonPayload);
        log.info("Command published to: {}", topic);
    }

    // Send command to ESP32
    public void publishCommand(String deviceId, String jsonsPayload){
        String topic = String.format(commandTopicTemplate, deviceId);
        sendMessage(topic, jsonsPayload);
        log.info("Command published to: {}", topic);
    }

    // Internal send methods that all of the above publish messages use
    private void sendMessage (String topic, String payload){
        mqttTemplate().send(
                mqttOutboundChannel(),
                MessageBuilder
                        .withPayload(payload)
                        .setHeader("mqtt_topic", topic)
                        .build()
        );
    }
}