#include "config.h"
#include "system_state.h"
#include "display_handler.h"


DisplayHandler::DisplayHandler() : _lcd(LCD_ADDRESS, LCD_COLS, LCD_ROWS), _currentNode(0), _lastSwitch(0) {

}

void DisplayHandler::initDisplay() {
    _lcd.init();
    _lcd.backlight();
    _lcd.clear();
    _lcd.setCursor(0, 0);
    _lcd.print("Initializing...");
}


void DisplayHandler::updateDisplay(SystemState& state) {
    unsigned long currentTime = millis();
    if (currentTime - _lastSwitch >= LCD_CYCLE_MS) {
        _lastSwitch = currentTime;
        _currentNode = (_currentNode + 1) % 4; // Cycle through 0, 1, 2, 3
        _lcd.clear();
    }

    _lcd.clear();
    _lcd.setCursor(0, 0);
    switch (_currentNode) {
        case 0:
            _lcd.print("Node A:");
            _lcd.setCursor(0, 1);
            _lcd.print("P:");
            _lcd.print(state.nodeAPressure, 1);
            _lcd.print(" V:");
            _lcd.print(state.velocityA, 1);
            break;
        case 1:
            _lcd.print("Node B:");
            _lcd.setCursor(0, 1);
            _lcd.print("P:");
            _lcd.print(state.nodeBPressure, 1);
            _lcd.print(" V:");
            _lcd.print(state.velocityB, 1);
            break;
        case 2:
            _lcd.print("Node C:");
            _lcd.setCursor(0, 1);
            _lcd.print("P:");
            _lcd.print(state.nodeCPressure, 1);
            _lcd.print(" V:");
            _lcd.print(state.velocityC, 1);
            break;
        case 3:
            _lcd.print("T:");
            _lcd.print(state.temperature, 1);
            _lcd.print("C H:");
            _lcd.print(state.humidity, 1);
            _lcd.print("%");
            _lcd.setCursor(0, 1);
            _lcd.print(state.faultStatus);
            break;
    }
}