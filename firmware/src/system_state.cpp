#include "config.h"
#include "system_state.h"


void initSystemState(SystemState& state) {
    state.nodeAPressure = 0.0f;
    state.velocityA = 0.0f;
    state.nodeBPressure = 0.0f;
    state.velocityB = 0.0f;
    state.nodeCPressure = 0.0f;
    state.velocityC = 0.0f;
    state.currentTimestep = 0;
    strncpy(state.currentScenario, "NORMAL_BASELINE", sizeof(state.currentScenario));
    state.temperature = 0.0f;
    state.humidity = 0.0f;
    strncpy(state.ledColor, "GREEN", sizeof(state.ledColor));
    strncpy(state.faultStatus, "NORMAL", sizeof(state.faultStatus));
    state.mqttConnected = false;
    state.wifiConnected = false;
    state.publishCount = 0;
    state.mutex = xSemaphoreCreateMutex();
}