package io.github.darlene.leakdetectionapplication.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class MqttHealthCheck {

    private final MqttPahoMessageDrivenChannelAdapter inboundAdapter;

    public MqttHealthCheck(MqttPahoMessageDrivenChannelAdapter inboundAdapter) {
        this.inboundAdapter = inboundAdapter;
    }

    @Scheduled(fixedDelay = 30000)
    public void ensureConnected() {
        try {
            if (!inboundAdapter.isRunning()) {
                log.warn("MQTT adapter not running - attempting restart...");
                inboundAdapter.start();
                log.info("MQTT adapter restarted successfully");
            } else {
                log.debug("MQTT adapter running OK");
            }
        } catch (Exception e) {
            log.error("Failed to restart MQTT adapter: {}", e.getMessage());
        }
    }
}