#include <Arduino.h>
#include "mqtt_handler.h"
#include "config.h"
#include "freertos/FreeRTOS.h"
#include "freertos/queue.h"
#include <ArduinoJson.h>
#include <MycilaMQTT.h>
#include "system_types.h"
#include <string>


MqttHandler::MqttHandler(QueueHandle_t ledQueue){
    _ledCommandQueue = ledQueue;
}


void MqttHandler::initMqtt(){
    _mqttClient.onConnect([](){
        Serial.println("MQTT Connected!");
    });

    _mqttClient.subscribe(TOPIC_LED_STATUS,[this](const std::string& topic, const std::string_view& payload){
        _handleMessage(topic, payload);
    });
    
}

bool MqttHandler::connectMqtt(){
    Mycila::MQTT::Config config;
    config.server = MQTT_BROKER;
    config.port = MQTT_PORT;
    config.clientId = MQTT_CLIENT_ID;
    config.username = MQTT_USERNAME;
    config.password = MQTT_PASSWORD;

    _mqttClient.begin(config);
    return _mqttClient.isConnected();
}

void MqttHandler::publishSensorReading(SensorReading reading){

    StaticJsonDocument<256> doc;
    doc["device_id"] = reading.deviceId;
    doc["nodeA_pressure"] = reading.nodeAPressure;
    doc["velocityA"] = reading.velocityA;
    doc["nodeB_pressure"] = reading.nodeBPressure;
    doc["velocityB"] = reading.velocityB;
    doc["nodeC_pressure"] = reading.nodeCPressure;
    doc["velocityC"] = reading.velocityC;
    char buffer[256];
    serializeJson(doc, buffer);
    _mqttClient.publish(TOPIC_SENSOR_DATA, buffer);

}


void MqttHandler::publishHeartBeat(){
    StaticJsonDocument<128> doc;
    doc["device_id"] = MQTT_CLIENT_ID;
    doc["timestamp"] = millis();
    doc["status"] = "alive";
    doc["publish_count"] = _publishCount;
    doc["error_count"] = _errorCount;

    char buffer[128];

    serializeJson(doc, buffer);
    _mqttClient.publish(TOPIC_HEARTBEAT, buffer);

}


void MqttHandler::maintainConnection(){
    // Check MQTT connection status
    if (!_mqttClient.isConnected()){
        Serial.println("MQTT disconnected, attempting to reconnect......");
        if (connectMqtt()){
            Serial.println("MQTT reconnected successfully!");
        } else {
            Serial.println("MQTT reconnection failed.");
        }
    }

}

void MqttHandler::_handleMessage(const std::string& topic, const std::string_view& payload){
        Serial.printf("[MQTT] %s -> %s\n", std::string(topic).c_str(), std::string(payload).c_str());

    StaticJsonDocument<128> doc;
    DeserializationError err = deserializeJson(doc, payload);

    if (err) {
        _errorCount++;
        return;
    }

    const char* color = doc["color"];
    if (color == nullptr) {
        _errorCount++;
        return;
    }

    std::string colorStr(color);
    if (xQueueSend(_ledCommandQueue, &colorStr, 0) != pdTRUE) {
        Serial.println("[MQTT] LED queue full");
    }
}