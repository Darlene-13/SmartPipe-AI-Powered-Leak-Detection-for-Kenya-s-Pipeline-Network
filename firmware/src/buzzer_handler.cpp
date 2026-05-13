#include <Arduino.h>
#include "buzzer_handler.h"
#include "config.h"

BuzzerHandler::BuzzerHandler() : _buzzerPin(PIN_BUZZER) {

}


void BuzzerHandler::initBuzzer(){
    pinMode(_buzzerPin, OUTPUT);
    digitalWrite(_buzzerPin, LOW);

}

void BuzzerHandler::alertLeak(){
    for(int i = 0; i <3; i++){
        digitalWrite(_buzzerPin, HIGH);
        vTaskDelay(200 / portTICK_PERIOD_MS);
        digitalWrite(_buzzerPin, LOW);
        vTaskDelay(200 / portTICK_PERIOD_MS);
    }
}


void BuzzerHandler::alertBlockage(){
    for(int i = 0; i <2; i++){
        digitalWrite(_buzzerPin, HIGH);
        vTaskDelay(100 / portTICK_PERIOD_MS);
        digitalWrite(_buzzerPin, LOW);
        vTaskDelay(100 / portTICK_PERIOD_MS);
    }
}

