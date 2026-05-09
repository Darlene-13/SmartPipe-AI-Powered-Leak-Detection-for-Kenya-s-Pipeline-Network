#pragma once
#include <Arduino.h>
#include "wifi_manager.h"
// Struct to group related data together for mqtt handler to use.
struct SensorReading{

    String deviceId;
    double nodeAPressure;
    double nodeBPressure;
    double nodeCPressure;
    double velocityA;
    double velocityB;
    double velocityC;
    
};