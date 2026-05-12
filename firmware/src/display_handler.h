#pragma once

#include "config.h"
#include "system_state.h"
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <Arduino.h>

class DisplayHandler {
    public:
        DisplayHandler();
        void initDisplay();
        void updateDisplay(SystemState& state);

    private:
        LiquidCrystal_I2C _lcd;
        uint8_t _currentNode;
        unsigned long _lastSwitch;
};