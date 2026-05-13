#pragma once

#include <Arduino.h>
#include "config.h"

class BuzzerHandler{
    public:
        BuzzerHandler();
        void initBuzzer();
        void alertLeak();
        void alertBlockage();

    private:
        int _buzzerPin;
};