#pragma once

#include <Arduino.h>
#include "freertos/FreeRTOS.h"
#include "freertos/queue.h"
#include <ArduinoJson.h>
#include <MycilaMQTT.h>
#include "system_types.h"
#include <string>
#undef MQTT  // guard against macro collision with MycilaMQTT.h



class MqttHandler{

    public:

        MqttHandler(QueueHandle_t ledQueue);

        void initMqtt();
        bool connectMqtt();
        void publishSensorReading(SensorReading reading);
        void publishHeartBeat();
        void maintainConnection();


    private:
        Mycila::MQTT _mqttClient;

        uint32_t _publishCount = 0;
        uint32_t _errorCount = 0;

        QueueHandle_t _ledCommandQueue;

        void _handleMessage(const std::string& topic, const std::string& payload);



};