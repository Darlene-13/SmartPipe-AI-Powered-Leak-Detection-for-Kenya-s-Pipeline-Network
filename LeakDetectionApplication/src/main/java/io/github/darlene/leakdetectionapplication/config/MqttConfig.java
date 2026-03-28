package io.github.darlene.leakdetectionapplication.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

/**
 * MqttConfig creates the connection factory used by both
 * MqttSubscriber and MqttPublisher.
 * Reads all connection settings from mqtt.properties.
 */
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

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(cleanSession);
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);
        return factory;
    }
}