#pragma once

#include "config.h"
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <Arduino.h>


class DisplayHandler {
    public:
        DisplayHandler();
        void initDisplay();
        void updateDisplay(float temperature, const char* status);

    private:
        LiquidCrystal_I2C _lcd;
        uint8_t _currentNode;

};