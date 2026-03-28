package io.github.darlene.leakdetectionapplication.mqtt;

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
import java.util.UUID;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class MqttSubscriber {

    private final MqttPahoClientFactory mqttClientFactory;

    @Value("${mqtt.subscribe.topics}")
    private String[] subscribeTopics;

    public MqttSubscriber(MqttPahoClientFactory mqttClientFactory) {
        this.mqttClientFactory = mqttClientFactory;
    }

    @Bean
    public ThreadPoolTaskExecutor mqttExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1); // Start with one thread
        executor.setMaxPoolSize(5); // Scale upto 5 if the queue gets full...
        executor.setQueueCapacity(500); // Limitin the backlog
        executor.setThreadNamePrefix("mqtt-proc-");
        executor.setKeepAliveSeconds(60);
        executor.initialize();
        return executor;
    }
    @Bean
    public MessageChannel mqttInputChannel(ThreadPoolTaskExecutor mqttExecutor ) {

        ExecutorChannel channel = new ExecutorChannel(mqttExecutor);

        channel.addInterceptor(new ChannelInterceptor() {
            @Override
            public org.springframework.messaging.Message<?> preSend(
                    org.springframework.messaging.Message<?> message,
                    MessageChannel ch) {
                System.out.println("=== CHANNEL INTERCEPTOR HIT ===");
                System.out.println("Topic: " + message.getHeaders()
                        .get("mqtt_receivedTopic"));
                System.out.println("Payload: " + message.getPayload());

                String topic = (String) message.getHeaders()
                        .get("mqtt_receivedTopic");
                String payload = message.getPayload().toString();

                if (topic == null) {
                    log.warn("Null topic received");
                    return message;
                }

                if (topic.equals("pipeline/devices/register")) {
                    log.info("Device registration - Payload: {}", payload);

                } else if (topic.matches("pipeline/sensors/.+/node")) {
                    log.info("Sensor reading - Topic: {}, Payload: {}",
                            topic, payload);

                } else if (topic.matches("pipeline/sensors/.+/status")) {
                    log.info("Heartbeat - Topic: {}, Payload: {}",
                            topic, payload);

                } else if (topic.matches("pipeline/sensors/.+/diagnostic")) {
                    log.warn("Diagnostic - Topic: {}, Payload: {}",
                            topic, payload);

                } else {
                    log.warn("Unknown topic: {}", topic);
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
}