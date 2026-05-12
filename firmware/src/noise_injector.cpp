#include "config.h"
#include "noise_injector.h"
#include <Arduino.h>


void NoiseInjector::initNoiseInjector(){
    pinMode(PIN_NOISE_INJECTOR, INPUT);
}

void NoiseInjector::injectNoise(float& value){
    int potValue = analogRead(PIN_NOISE_INJECTOR);
    float noise = (potValue / 4095.0 - 0.5) * 2 * NOISE_SCALE_FACTOR;
    value += noise;

}