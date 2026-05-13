#pragma once

#include "config.h"
#include <Arduino.h>

class NoiseInjector {
    public:
        void initNoiseInjector();
        void injectNoise(float& value);

};