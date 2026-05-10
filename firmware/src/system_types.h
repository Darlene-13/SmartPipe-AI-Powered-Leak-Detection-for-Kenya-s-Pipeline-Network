#pragma once
#include <Arduino.h>
#include "wifi_manager.h"

struct SensorReading{

    String deviceId;
    double nodeAPressure;
    double velocityA;
    double nodeBPressure;
    double velocityB;
    double nodeCPressure;
    double velocityC;
    
};