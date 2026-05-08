#include "wifi_manager.h"
#include "config.h"

// Credentials come from config.h which is in .gitignore
// Never hardcode them here
WifiManager wifi(WIFI_SSID, WIFI_PASSWORD);

void setup() {
    Serial.begin(115200);

    if (!wifi.connect()) {

        Serial.println("[Main] WiFi failed. Will retry via maintainConnection.");
    } else {
        Serial.print("[Main] IP: ");
        Serial.println(wifi.getIPAddress());
        Serial.print("[Main] Signal: ");
        Serial.print(wifi.getSignalStrength());
        Serial.println(" dBm");
    }
}

void loop() {
    // This loop() won't exist once FreeRTOS tasks take over.
    // For now it lets you test wifi_manager in isolation.
    wifi.maintainConnection();
    vTaskDelay(1000 / portTICK_PERIOD_MS);
}