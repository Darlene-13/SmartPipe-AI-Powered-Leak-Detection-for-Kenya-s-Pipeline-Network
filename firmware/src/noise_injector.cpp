#include "config.h"
#include "noise_injector.h"
#include <Arduino.h>


void NoiseInjector::initNoiseInjector(){
    pinMode(PIN_POT, INPUT);
}

void NoiseInjector::injectNoise(float& value) {
    int potValue = analogRead(PIN_POT);
    float noiseFraction = (potValue / 4095.0f) * NOISE_SCALE_FACTOR;
    float noiseAmount = value * noiseFraction;
    int sign = (random(2) == 0) ? 1 : -1;
    value += noiseAmount * sign;
}