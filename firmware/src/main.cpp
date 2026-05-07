#include "wifi_manager.h"

WifiManager wifi("MySSID", "MyPassword");

void setup(){

    Serial.begin(115200);

    if(!wifi.connect()){
        Serial.println("Failed to connect.");
        wifi.startAccessPoint();
    } else {
        Serial.println("Connected!");
        Serial.println(wifi.getIPAddress());
    }
}

void loop(){
    wifi.maintainConnection();

    delay(1000);
}