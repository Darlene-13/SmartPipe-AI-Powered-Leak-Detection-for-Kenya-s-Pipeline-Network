#include <Arduino.h>
#include "config.h"

void setup() {
    Serial.begin(115200);
    pinMode(PIN_LED_RED,   OUTPUT);
    pinMode(PIN_LED_GREEN, OUTPUT);
    pinMode(PIN_LED_BLUE,  OUTPUT);
    Serial.println("LED test starting...");
}

void loop() {
    digitalWrite(PIN_LED_RED,   HIGH);
    digitalWrite(PIN_LED_GREEN, LOW);
    digitalWrite(PIN_LED_BLUE,  LOW);
    Serial.println("RED");
    delay(1000);

    digitalWrite(PIN_LED_RED,   LOW);
    digitalWrite(PIN_LED_GREEN, HIGH);
    digitalWrite(PIN_LED_BLUE,  LOW);
    Serial.println("GREEN");
    delay(1000);

    digitalWrite(PIN_LED_RED,   LOW);
    digitalWrite(PIN_LED_GREEN, LOW);
    digitalWrite(PIN_LED_BLUE,  HIGH);
    Serial.println("BLUE");
    delay(1000);
}