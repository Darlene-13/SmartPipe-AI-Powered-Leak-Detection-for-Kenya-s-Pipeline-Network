#pragma once
#include <Arduino.h>
#include "system_types.h"
#include "data/normal_1.h"
#include "data/normal_2.h"
#include "data/normal_3.h"
#include "data/leak_incipient.h"
#include "data/leak_moderate.h"
#include "data/leak_critical.h"
#include "data/blockage_25.h"
#include "data/blockage_50.h"
#include "data/blockage_75.h"

class DataReplayer {
    public:
        DataReplayer();
        void initReplayer();
        SensorReading getNextReading();
        void setScenario(const char* scenarioName);

    private:
        uint16_t _currentTimestep;
        char _currentScenario[32];
        const float (*_activeData)[6];
        uint16_t _activeLen;
};