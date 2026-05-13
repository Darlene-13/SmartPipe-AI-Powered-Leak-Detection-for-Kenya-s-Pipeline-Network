#pragma once
#include <Arduino.h>
#include "system_state.h"


class DataReplayer{
    public:
        DataReplayer();
        void initReplayer();
        void getNextReading();
        int counter();
        void setScenarioName();


    private:

        uint16_t _currentTimestep;
        char _currentScenario[32];
};