#pragma once
#include <Arduino.h>
#include "wifi_manager.h"
// Struct to group related data together for mqtt handler to use.
struct SensorReading{

    String deviceId;
    double nodeAPressure;
    double velocityA;
    double nodeBPressure;
    double velocityB;
    double nodeCPressure;
    double velocityC;
    uint32_t timestep;
    String scenario;
};

