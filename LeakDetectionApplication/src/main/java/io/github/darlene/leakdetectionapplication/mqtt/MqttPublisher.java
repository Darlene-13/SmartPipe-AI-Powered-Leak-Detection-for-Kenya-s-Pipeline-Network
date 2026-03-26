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


@Configuration
public class MqttPublisher {

    private final MqttPahoClientFactory mqttClientFactory;

    @Value("${topic}")
    private String defaultTopic;

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
        handler.setDefautTopic(defaultTopic);

        return handler;
    }

    // Method our services will call to publish
    @Bean
    public MessagingTemplate mqttTemplate(){
        return new MessagingTemplate(
                mqttOutboundChannel()
        );
    }

}