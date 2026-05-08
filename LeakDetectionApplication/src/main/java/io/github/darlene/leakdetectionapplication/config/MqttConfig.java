package io.github.darlene.leakdetectionapplication.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;

/**
 * MQTT connection and topic configuration.
 * Creates the shared MqttPahoClientFactory used by MqttSubscriber and MqttPublisher.
 * Exposes all topic names as Spring beans for injection across the application.
 * Connects to HiveMQ Cloud via SSL on port 8883.
 */
@Slf4j
@Getter
@Configuration
@PropertySource("classpath:mqtt.properties")
public class MqttConfig {

    @Value("${mqtt.broker}")
    private String brokerUrl;

    @Value("${mqtt.clientId}")
    private String clientId;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.cleanSession}")
    private boolean cleanSession;

    @Value("${mqtt.connectionTimeout}")
    private int connectionTimeout;

    @Value("${mqtt.keepAliveInterval}")
    private int keepAliveInterval;

    @Value("${mqtt.qos}")
    private int qos;

    @Value("${mqtt.subscribe.topics}")
    private String subscribeTopics;

    @Value("${mqtt.topics.sensor-data}")
    private String sensorDataTopic;

    @Value("${mqtt.topics.heartbeat}")
    private String heartbeatTopic;

    @Value("${mqtt.topics.led-status}")
    private String ledStatusTopic;

    @Value("${mqtt.topics.cmd-scenario}")
    private String cmdScenarioTopic;

    @Value("${mqtt.topics.system-status}")
    private String systemStatusTopic;

    @Value("${mqtt.topics.new-alert}")
    private String newAlertTopic;


    @Value("${mqtt.publish.topic.config}")
    private String deviceConfigTopicTemplate;

    @Value("${mqtt.publish.topic.alerts}")
    private String alertsTopicTemplate;

    @Value("${mqtt.publish.topic.commands}")
    private String commandsTopicTemplate;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(cleanSession);
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setAutomaticReconnect(true);

        // SSL configuration for HiveMQ Cloud
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            options.setSocketFactory(sslContext.getSocketFactory());
            log.info("SSL configured for HiveMQ Cloud connection");
        } catch (NoSuchAlgorithmException | java.security.KeyManagementException e) {
            log.error("Failed to configure SSL for MQTT: {}", e.getMessage());
            throw new RuntimeException("MQTT SSL configuration failed", e);
        }

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);

        log.info("MQTT client factory configured for broker: {}", brokerUrl);
        return factory;
    }

    @Bean(name = "subscribeTopicList")
    public String[] subscribeTopicList() {
        return subscribeTopics.split(",");
    }

    @Bean(name = "sensorDataTopic")
    public String sensorDataTopic() {
        return sensorDataTopic;
    }

    @Bean(name = "heartbeatTopic")
    public String heartbeatTopic() {
        return heartbeatTopic;
    }

    @Bean(name = "ledStatusTopic")
    public String ledStatusTopic() {
        return ledStatusTopic;
    }

    @Bean(name = "cmdScenarioTopic")
    public String cmdScenarioTopic() {
        return cmdScenarioTopic;
    }

    @Bean(name = "systemStatusTopic")
    public String systemStatusTopic() {
        return systemStatusTopic;
    }
    @Bean(name = "newAlertTopic")
    public String newAlertTopic() {
        return newAlertTopic;
    }
    @Bean(name = "deviceConfigTopicTemplate")
    public String deviceConfigTopicTemplate() {
        return deviceConfigTopicTemplate;
    }
    @Bean(name = "alertsTopicTemplate")
    public String alertsTopicTemplate() {
        return alertsTopicTemplate;
    }

    @Bean(name = "commandsTopicTemplate")
    public String commandsTopicTemplate() {
        return commandsTopicTemplate;
    }

    public String buildDeviceConfigTopic(String deviceId) {
        return String.format(deviceConfigTopicTemplate, deviceId);
    }

    public String buildAlertsTopic(String deviceId) {
        return String.format(alertsTopicTemplate, deviceId);
    }

    public String buildCommandsTopic(String deviceId) {
        return String.format(commandsTopicTemplate, deviceId);
    }
}