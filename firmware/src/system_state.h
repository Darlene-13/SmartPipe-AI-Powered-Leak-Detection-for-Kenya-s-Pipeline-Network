#pragma once
#include <Arduino.h>
#include "freertos/FreeRTOS.h"
#include "freertos/semphr.h"


struct SystemState {
    // Pressure and velocity from data_replayer
    float nodeAPressure;
    float velocityA;
    float nodeBPressure;
    float velocityB;
    float nodeCPressure;
    float velocityC;
    uint32_t currentTimestep;
    char currentScenario[32];

    // From DHT reader
    float temperature;
    float humidity;

    // From MQTT/ML
    char ledColor[32];
    char faultStatus[32];

    // System health
    bool mqttConnected;
    bool wifiConnected;
    uint32_t publishCount;

    // The rubber duck
    SemaphoreHandle_t mutex;
};


void initSystemState(SystemState& state);