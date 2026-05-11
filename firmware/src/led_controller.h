#pragma once

#include <Arduino.h>
#include "config.h"


class LedController {

    public:
        void initLed();
        void setColor(const char* color);

    private:
        char currentColor[32]; // Assuming a maximum length for the color string
};