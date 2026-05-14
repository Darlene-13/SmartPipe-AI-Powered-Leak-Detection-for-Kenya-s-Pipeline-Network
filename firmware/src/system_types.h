#pragma once
#include <Arduino.h>
#include "freertos/FreeRTOS.h"
#include "freertos/semphr.h"


struct SensorReading {
    // Pressure and velocity from data_replayer
    float nodeAPressure;
    float velocityA;
    float nodeBPressure;
    float velocityB;
    float nodeCPressure;
    float velocityC;
    uint32_t currentTimestep;
    char currentScenario[32];

};