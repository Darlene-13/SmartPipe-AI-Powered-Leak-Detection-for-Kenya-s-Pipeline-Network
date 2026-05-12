#include "config.h"
#include "noise_injector.h"
#include <Arduino.h>


void NoiseInjector::initNoiseInjector(){
    pinMode(PIN_POT, INPUT);
}

void NoiseInjector::injectNoise(float& value){
    int potValue = analogRead(PIN_POT);
    float noise = (potValue / 4095.0 - 0.5) * 2 * NOISE_SCALE_FACTOR;
    value += noise;

}