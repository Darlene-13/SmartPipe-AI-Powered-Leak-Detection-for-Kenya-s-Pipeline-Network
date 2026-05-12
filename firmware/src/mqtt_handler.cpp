#include "mqtt_handler.h"

MqttHandler::MqttHandler(QueueHandle_t ledQueue) {
    _ledCommandQueue = ledQueue;
}

void MqttHandler::initMqtt() {
    _mqttClient.setInsecure();
    _mqttClient.setServer(MQTT_BROKER, MQTT_PORT);
    _mqttClient.setCredentials(MQTT_USERNAME, MQTT_PASSWORD);
    _mqttClient.setClientId(MQTT_CLIENT_ID);

    _mqttClient.onConnect([](bool sessionPresent) {
        Serial.println("[MQTT] Connected to broker.");
    });

    _mqttClient.onDisconnect([](espMqttClientTypes::DisconnectReason reason) {
        Serial.printf("[MQTT] Disconnected. Reason: %d\n", (int)reason);
    });

    _mqttClient.onMessage(
        [this](const espMqttClientTypes::MessageProperties& props,
               const char* topic,
               const uint8_t* payload,
               size_t len,
               size_t index,
               size_t total) {
            _onMessage(props, topic, payload, len, index, total);
        }
    );
}

bool MqttHandler::connectMqtt() {
    _mqttClient.connect();

    int timeout = WIFI_TIMEOUT_MS / 500;
    while (!_mqttClient.connected() && timeout > 0) {
        _mqttClient.loop();
        vTaskDelay(500 / portTICK_PERIOD_MS);
        Serial.print(".");
        timeout--;
    }
    Serial.println();

    if (_mqttClient.connected()) {
        _mqttClient.subscribe(TOPIC_LED_STATUS, 0);
        Serial.println("[MQTT] connectMqtt() success.");
        return true;
    }

    Serial.println("[MQTT] connectMqtt() timed out.");
    _errorCount++;
    return false;
}

void MqttHandler::publishSensorReading(SystemState reading) {
    JsonDocument doc;
    doc["device_id"]      = MQTT_CLIENT_ID;
    doc["timestamp"]      = millis();
    doc["nodeA_pressure"] = reading.nodeAPressure;
    doc["velocityA"]      = reading.velocityA;
    doc["nodeB_pressure"] = reading.nodeBPressure;
    doc["velocityB"]      = reading.velocityB;
    doc["nodeC_pressure"] = reading.nodeCPressure;
    doc["velocityC"]      = reading.velocityC;
    doc["scenario"]       = reading.currentScenario;
    doc["timestep"]       = reading.currentTimestep;

    char buffer[256];
    serializeJson(doc, buffer);

    _mqttClient.publish(TOPIC_SENSOR_DATA, 0, false, buffer);
    _publishCount++;
}

void MqttHandler::publishHeartbeat() {
    JsonDocument doc;
    doc["device_id"]     = MQTT_CLIENT_ID;
    doc["uptime_ms"]     = millis();
    doc["publish_count"] = _publishCount;
    doc["error_count"]   = _errorCount;

    char buffer[128];
    serializeJson(doc, buffer);

    _mqttClient.publish(TOPIC_HEARTBEAT, 0, false, buffer);
}

void MqttHandler::maintainConnection() {
    _mqttClient.loop();

    if (!_mqttClient.connected()) {
        Serial.println("[MQTT] Disconnected. Reconnecting...");
        connectMqtt();
    }
}

void MqttHandler::_onMessage(
    const espMqttClientTypes::MessageProperties& props,
    const char* topic,
    const uint8_t* payload,
    size_t len,
    size_t index,
    size_t total)
{
    Serial.printf("[MQTT] Message on %s\n", topic);

    JsonDocument doc;
    DeserializationError err = deserializeJson(doc, payload, len);

    if (err) {
        Serial.println("[MQTT] JSON parse failed.");
        _errorCount++;
        return;
    }

    const char* color = doc["color"];
    if (color == nullptr) {
        Serial.println("[MQTT] No color field.");
        _errorCount++;
        return;
    }

    char colorBuf[32];
    strncpy(colorBuf, color, sizeof(colorBuf) - 1);
    colorBuf[sizeof(colorBuf) - 1] = '\0';

    if (xQueueSend(_ledCommandQueue, colorBuf, 0) != pdTRUE) {
        Serial.println("[MQTT] LED queue full.");
        _errorCount++;
    }
}