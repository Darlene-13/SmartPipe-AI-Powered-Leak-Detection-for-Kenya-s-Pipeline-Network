#pragma once

#include <Arduino.h>
#include "config.h"
#include "freertos/FreeRTOS.h"
#include "freertos/queue.h"
#include <ArduinoJson.h>
#include <espMqttClient.h>
#include "system_state.h"

class MqttHandler {
    public:
        MqttHandler(QueueHandle_t ledQueue);

        void initMqtt();
        bool connectMqtt();
        void publishSensorReading(SensorReading reading);
        void publishHeartbeat();
        void maintainConnection();

    private:
        espMqttClientSecure _mqttClient;

        uint32_t _publishCount = 0;
        uint32_t _errorCount   = 0;

        QueueHandle_t _ledCommandQueue;

        void _onMessage(const espMqttClientTypes::MessageProperties& props,
                        const char* topic,
                        const uint8_t* payload,
                        size_t len,
                        size_t index,
                        size_t total);
};