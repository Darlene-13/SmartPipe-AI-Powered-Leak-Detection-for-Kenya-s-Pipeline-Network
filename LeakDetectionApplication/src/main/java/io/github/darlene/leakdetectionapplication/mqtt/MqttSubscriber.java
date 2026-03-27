package io.github.darlene.leakdetectionapplication.mqtt;

// Spring framework
// Those that creates beans for Dependecy injection.
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Reading the topic value from mqtt properties
import org.springframework.beans.factory.annoation.Value;
// Channel the pipe between adapter and handler
import org.springframework.integration.channel.DirectChannel;
import org.springframework.message.MessageChannel;
// Mqtt listener
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;

// Factory from which mqttconfig is injected
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
// The handler that processed received messages
import org.springframework.messaging.MessageHandler;

// Import UUID
import java.util.UUID;

// lombok
import lombok.Slf4j;

@Slf4j
@Configuration
public class MqttSubscriber{

    // Injected from mqttconfig the connection factory
    private final MqttPahoClientFactory mqttClientFactory;

    // String to subscribe to all the 4 topics at once.
    @Value("${subscribe.topics}")
    private String [] subscribeTopics;

    // Constructor injection which is preffered over autowired in field injection.
    public MqttSubscriber (MqttPahoClientFactory mqttClientFactory){
        this.mqttClientFactory = mqttClientFactory;
    }

    // Bean 1 The channel (pipe)
    @Bean
    public MessageChannel mqttInputChannel(){
        return new DirectChannel();
    }

    // Bean 2 The actual MQTT listener
    @Bean
    public MqttPahoMessageDrivenChannelAdapter inboundAdapter (){

        // UUID to ensure that the client ID never collides
        String subscriberClientId = "subscriber-" + UUID.randomUUID();

        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                subscriberClientId,
                mqttClientFactory,
                subscribeTopics
                );
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    // Bean 3 What to do when a message arrives
    @Bean
    public MessageHandler mqttMessageHandler(){
        return message ->{
            // Which exact topic did this come from.
            // ie: pipeline/sensors/node
            String topic = (string) message
                    .getHeaders()
                    .get("mqtt_receivedTopic");

            // The raw Json string from ESP32
            String payload = (string) message.getPayload();

            //Processing service handler or we could print out.
            System.out.println("Topic  :" + topic);
            System.out.println("'Payload' :" + payload);
            System.out.println("------------------------");

            // To be commented as it specifically is for the service files
            if (topic == null){
                log.warn("Received message with null topic");
                return;
            } else if (topic.matches("pipeline/sensors/.+/node")){
                log.info("Sensor reading received");
                log.info("Topic     :  {}", topic);
                log.info("Payload   :  {}", payload);
                log.warn("------------------------");
            } else if (topic.matches("pipeline/sensors/.+/status")){
                log.info("Device hearbeat recived");
                log.info("Topic     :  {}", topic);
                log.info("Payload   :  {}", payload);
                log.warn("------------------------");
            } else if (topic.matches("pipeline/sensors/.+/diagnostic")){
                log.info("Diagnostic message received");
                log.info("Topic     :  {}", topic);
                log.info("Payload   :  {}", payload);
                log.warn("------------------------");
            } else {
                log.warn("Unknown topic received: {}", topic);
            }
        };
    }
}