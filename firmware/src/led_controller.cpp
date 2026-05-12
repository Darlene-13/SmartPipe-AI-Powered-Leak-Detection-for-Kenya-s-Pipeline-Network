#include <Arduino.h>
#include "led_controller.h"
#include "config.h"

char _currentColor[32] = "";

void LedController::initLed(){
    pinMode(PIN_LED_RED, OUTPUT);
    pinMode(PIN_LED_GREEN, OUTPUT);
    pinMode(PIN_LED_BLUE, OUTPUT);
    setColor("GREEN"); // Default to green on startup
}

void LedController::setColor(const char* color){
    // Skip if the color is the same as the current color
    if(strcmp(_currentColor, color) == 0){
        return;
    }

    strcpy(_currentColor, color); // Update the current color

    if (strcmp(color, "GREEN") == 0) {
        digitalWrite(PIN_LED_BLUE, LOW);
        digitalWrite(PIN_LED_RED, LOW);
        digitalWrite(PIN_LED_GREEN, HIGH);
    } else if (strcmp(color, "RED") == 0) {
        digitalWrite(PIN_LED_BLUE, LOW);
        digitalWrite(PIN_LED_RED, HIGH);
        digitalWrite(PIN_LED_GREEN, LOW);
    } else if (strcmp(color, "BLUE") == 0) {
        digitalWrite(PIN_LED_BLUE, HIGH);
        digitalWrite(PIN_LED_RED, LOW);
        digitalWrite(PIN_LED_GREEN, LOW);
    } else if (strcmp(color, "YELLOW") == 0){
        digitalWrite(PIN_LED_BLUE, LOW);
        digitalWrite(PIN_LED_RED, HIGH);
        digitalWrite(PIN_LED_GREEN, HIGH);
    }  else if (strcmp(color, "WHITE") == 0) {
    digitalWrite(PIN_LED_RED,   HIGH);
    digitalWrite(PIN_LED_GREEN, HIGH);
    digitalWrite(PIN_LED_BLUE,  HIGH);
    }
    else {
        // Invalid color - turn off all LEDs
        digitalWrite(PIN_LED_BLUE, LOW);
        digitalWrite(PIN_LED_RED, LOW);
        digitalWrite(PIN_LED_GREEN, LOW);
    }
}