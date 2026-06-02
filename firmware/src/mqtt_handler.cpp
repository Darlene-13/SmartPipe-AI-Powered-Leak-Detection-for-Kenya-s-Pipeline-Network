#include "mqtt_handler.h"
#include "system_types.h"

static const char* HIVEMQ_ROOT_CA = R"(
-----BEGIN CERTIFICATE-----
MIIFGDCCBACgAwIBAgISBmYUym6wSVm+hVZYpdHL9J6cMA0GCSqGSIb3DQEBCwUA
MDMxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MQwwCgYDVQQD
EwNSMTMwHhcNMjYwNDE3MTUyOTAwWhcNMjYwNzE2MTUyODU5WjAfMR0wGwYDVQQD
DBQqLnMxLmV1LmhpdmVtcS5jbG91ZDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCC
AQoCggEBAKVuz2sMPmxx2w/f81/YAEKTbNZMJPk2+ooLFg5hxXvReF+AwIT4XvZ+
MLhSKvFxmghJF+BB9WyhqrcJLGDCP4s6SOLWTYixEoTcaLUviqqn+06kYqDJ6E83
NGsc7T42DlPnzqcZZjPRed9rt4CP3RgeZlWyYZgiD8FoJG9gie8ytihF/FkGZT8T
N4Vkl2vQa3mfBWeeKrcuhcLPxqIWDz/30iYfLtEe5JYYScoCKTXcP9SUStjpR8pD
vfOWdvasOAuBy7yBbx01/4lcQt50hfbhTR/K14/D4rNkuuvU7ktSQnoxVXC8YDwG
zkny10DFt65mVYLNZcBQtOLHHOZGV30CAwEAAaOCAjgwggI0MA4GA1UdDwEB/wQE
AwIFoDATBgNVHSUEDDAKBggrBgEFBQcDATAMBgNVHRMBAf8EAjAAMB0GA1UdDgQW
BBSCwSMNTfn4RYkoGwXEnSUzQ9cyLjAfBgNVHSMEGDAWgBTnq58PLDOgU9NeT3jI
soQOO9aSMzAzBggrBgEFBQcBAQQnMCUwIwYIKwYBBQUHMAKGF2h0dHA6Ly9yMTMu
aS5sZW5jci5vcmcvMDMGA1UdEQQsMCqCFCouczEuZXUuaGl2ZW1xLmNsb3VkghJz
MS5ldS5oaXZlbXEuY2xvdWQwEwYDVR0gBAwwCjAIBgZngQwBAgEwLgYDVR0fBCcw
JTAjoCGgH4YdaHR0cDovL3IxMy5jLmxlbmNyLm9yZy8yOS5jcmwwggEOBgorBgEE
AdZ5AgQCBIH/BIH8APoAfwBGr4Y9Oz7ln6V33qgkXTaw2e0ioiP0YXdBIpRS7pVQ
XwAAAZ2cRNaaAAgAAAUABCNy7wQDAEgwRgIhAJlg4LRrt1M2dEQosi6wPWjET6yS
ekNxcg56fWOOQ9C8AiEAmmuPIYP28o97cRg1WGoW7fu6AWadHQseMdr6VxFi/ssA
dwDXbX0Q0af1d8LH6V/XAL/5gskzWmXh0LMBcxfAyMVpdwAAAZ2cRNZtAAAEAwBI
MEYCIQD+LETYtouBvzYygQwD2hljOk7185fa57jzzso2KMbV5wIhAMqcqKt1fZMr
9rY9s7PHEqQJYJFi7/UEybay9RwQeyBsMA0GCSqGSIb3DQEBCwUAA4IBAQB2t5O2
nZJ0i2cGoaD3h7FH2zNdgazMkgUMRG9WZg1CV4yciQXVGzmw894eAfTaPHNPjBgG
e9EUQxrdMP3vxvN1kRiKMXH6RyyFRg4jNKSFKVStSB9pMsjeZwEqXxQPwwqHWjPN
+9T7YVd+WgEyjN7+MpPaWtfPN9vTDkINLaiDA07oFWLr8/cZMRqxiORwAqGx1fhX
fth0PxXnvCpEIyn4ktWG6ah+uiA6OC3WStnh3mcPpxJBvwkvQ/xy1FPwkd7ZHfNU
drgNSdjtw/IQb2SQu6gf5x+TyQ9I448UKTceT8f6gDLHlde1pgaMqM74oNpiHzbN
fZZlNQFADj8GuTXf
-----END CERTIFICATE-----
)";

MqttHandler::MqttHandler(QueueHandle_t ledQueue) {
    _ledCommandQueue = ledQueue;
}

void MqttHandler::initMqtt() {
    _mqttClient.setCACert(HIVEMQ_ROOT_CA);
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

void MqttHandler::publishSensorReading(SensorReading reading) {
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