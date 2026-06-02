#include <Arduino.h>
#include "config.h"
#include "system_state.h"
#include "wifi_manager.h"
#include "mqtt_handler.h"
#include "data_replayer.h"
#include "noise_injector.h"
#include "dht_reader.h"
#include "led_controller.h"
#include "display_handler.h"
#include "buzzer_handler.h"

// Global objects
SystemState systemState;
QueueHandle_t ledCommandQueue;

WifiManager    wifi(WIFI_SSID, WIFI_PASSWORD);
MqttHandler*   mqttHandler;
DataReplayer   replayer;
NoiseInjector  noiseInjector;
DHTReader      dhtReader;
LedController  ledController;
DisplayHandler displayHandler;
BuzzerHandler  buzzerHandler;

void DataPublishTask(void* pvParameters) {
    unsigned long lastHeartbeat = 0;

    Serial.println("[DataPublish] Waiting for WiFi...");
    while (!wifi.isConnected()) {
        vTaskDelay(500 / portTICK_PERIOD_MS);
    }
    Serial.println("[DataPublish] WiFi ready, starting MQTT...");

    while (true) {
        wifi.maintainConnection();
        mqttHandler->maintainConnection();

        SensorReading reading = replayer.getNextReading();

        noiseInjector.injectNoise(reading.nodeAPressure);
        noiseInjector.injectNoise(reading.velocityA);
        noiseInjector.injectNoise(reading.nodeBPressure);
        noiseInjector.injectNoise(reading.velocityB);
        noiseInjector.injectNoise(reading.nodeCPressure);
        noiseInjector.injectNoise(reading.velocityC);

        if (xSemaphoreTake(systemState.mutex, pdMS_TO_TICKS(100)) == pdTRUE) {
            systemState.nodeAPressure   = reading.nodeAPressure;
            systemState.velocityA       = reading.velocityA;
            systemState.nodeBPressure   = reading.nodeBPressure;
            systemState.velocityB       = reading.velocityB;
            systemState.nodeCPressure   = reading.nodeCPressure;
            systemState.velocityC       = reading.velocityC;
            systemState.currentTimestep = reading.currentTimestep;
            strncpy(systemState.currentScenario, reading.currentScenario, 32);
            systemState.publishCount++;
            xSemaphoreGive(systemState.mutex);
        }

        mqttHandler->publishSensorReading(reading);

        if (millis() - lastHeartbeat >= HEARTBEAT_INTERVAL_MS) {
            mqttHandler->publishHeartbeat();
            lastHeartbeat = millis();
        }

        vTaskDelay(PUBLISH_INTERVAL_MS / portTICK_PERIOD_MS);
    }
}

void MqttReceiveTask(void* pvParameters) {
    char colorBuf[32];

    while (true) {
        if (xQueueReceive(ledCommandQueue, &colorBuf, pdMS_TO_TICKS(10)) == pdTRUE) {
            ledController.setColor(colorBuf);

            if (xSemaphoreTake(systemState.mutex, pdMS_TO_TICKS(100)) == pdTRUE) {
                strncpy(systemState.ledColor, colorBuf, 32);
                xSemaphoreGive(systemState.mutex);
            }

            if (strcmp(colorBuf, "RED") == 0)    buzzerHandler.alertLeak();
            if (strcmp(colorBuf, "YELLOW") == 0) buzzerHandler.alertLeak();
            if (strcmp(colorBuf, "BLUE") == 0)   buzzerHandler.alertBlockage();
        }

        vTaskDelay(10 / portTICK_PERIOD_MS);
    }
}

void DisplayTask(void* pvParameters) {
    while (true) {
        if (xSemaphoreTake(systemState.mutex, pdMS_TO_TICKS(100)) == pdTRUE) {
            displayHandler.updateDisplay(systemState);
            xSemaphoreGive(systemState.mutex);
        }
        vTaskDelay(DISPLAY_INTERVAL_MS / portTICK_PERIOD_MS);
    }
}

void SensorReadTask(void* pvParameters) {
    float temp, humidity;

    while (true) {
        if (dhtReader.readSensor(temp, humidity)) {
            if (xSemaphoreTake(systemState.mutex, pdMS_TO_TICKS(100)) == pdTRUE) {
                systemState.temperature = temp;
                systemState.humidity    = humidity;
                xSemaphoreGive(systemState.mutex);
            }
        }
        vTaskDelay(DHT_READ_INTERVAL_MS / portTICK_PERIOD_MS);
    }
}

void setup() {
    Serial.begin(115200);
    unsigned long t = millis();
    while (!Serial && (millis() - t < 5000));
    
    Serial.println("[Main] Booting...");
    initSystemState(systemState);
    ledCommandQueue = xQueueCreate(10, sizeof(char[32]));
    ledController.initLed();

    Serial.println("[Main] Connecting WiFi...");
    bool wifiOk = wifi.connect();
    Serial.print("[Main] WiFi result: ");
    Serial.println(wifiOk ? "CONNECTED" : "FAILED");

    if (!wifiOk) {
        Serial.println("[Main] Halting - no WiFi.");
        while(true) { vTaskDelay(1000 / portTICK_PERIOD_MS); }
    }

    replayer.initReplayer();
    mqttHandler = new MqttHandler(ledCommandQueue);
    mqttHandler->initMqtt();

    xTaskCreatePinnedToCore(DataPublishTask, "DataPublish", 12288, NULL, PRIORITY_HIGH, NULL, 1);
    xTaskCreatePinnedToCore(MqttReceiveTask, "MqttReceive", STACK_MQTT, NULL, PRIORITY_HIGH, NULL, 1);

    Serial.println("[Main] All tasks started.");
}

// ─── loop ─────────────────────────────────────────────────────────────────────
void loop() {
    vTaskDelay(portMAX_DELAY);
}