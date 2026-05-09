#pragma once
#include <Arduino.h>
#include "wifi_manager.h"

struct SensorReading{

    String deviceId;
    double nodeAPressure;
    double nodeBPressure;
    double nodeCPressure;
    double velocityA;
    double velocityB;
    double velocityC;
    
};