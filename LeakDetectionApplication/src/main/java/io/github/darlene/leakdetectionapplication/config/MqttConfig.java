package io.github.darlene.leakdetectionapplication.config;


// Spring framework
import org.springframework.contet.annoation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;


// Mqtt connection objects
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;

/**
 *  MqttConnectOptions - this is the object where we set the timeout, keepalive and clean session. It is like our connection settings bag
 *  DefaultMqttPahoClientFactory - this the actual factory that uses those options to create the mqtt client connections. Basically what our subscriber and Publisher will both receive
 *  MqttPahoClientFactory - this is the interface where we type return it keeps things loosely occupied
 */

@Configuration

@PropertySource("classpath:mqtt.properties")
public class MqttConfig{

    @Value("${broker}")
    private String brokerUrl;

    @Value("${clientId}")
    private String clientId;

    @Value("${username}")
    private String username;

    @Value("${password}")
    private String password;

    @Value("${cleanSession}")
    private boolean cleanSession;

    @Value("${connectionTimeout}")
    private int connectionTimeout;

    @Value("${keepAliveInternal}")
    private int keepAliveInternal;

    @Bean
    public MqttPahoClientFactory mqttClientFactory(){
        MqttConnectOptions options = new MqttConnectOptions();

        options.setServerURIs(new String[]{brokerUrl});
        options.setCleanSession(cleanSession);
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInternal);

        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);

        return factory;
    }

}