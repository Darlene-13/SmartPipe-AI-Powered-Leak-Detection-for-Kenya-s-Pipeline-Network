#include "wifi_manager.h"

WifiManager::WifiManager(const char* ssid, const char* password)
    : _ssid(ssid),
      _password(password),
      _lastReconnectAttempt(0),
      _reconnectAttempts(0) {

}

bool WifiManager::connect() {

    if (_ssid == nullptr || _password == nullptr) {
        Serial.println("[WiFi] ERROR: credentials are null");
        return false;
    }

    WiFi.mode(WIFI_STA);
    WiFi.begin(_ssid, _password);

    Serial.print("[WiFi] Connecting to.....");
    Serial.println(_ssid);

    int timeout = WIFI_TIMEOUT_MS / 500;  

    while (WiFi.status() != WL_CONNECTED && timeout > 0) {
        vTaskDelay(500 / portTICK_PERIOD_MS); 
        Serial.print(".");
        timeout--;
    }

    Serial.println();

    if (WiFi.status() == WL_CONNECTED) {
        _reconnectAttempts = 0;
        Serial.print("[WiFi] Connected. IP: ");
        Serial.println(WiFi.localIP());
        return true;
    }

    Serial.println("[WiFi] Connection failed.");
    return false;
}


void WifiManager::disconnect() {
    WiFi.disconnect();
    Serial.println("[WiFi] Disconnected.");
}


void WifiManager::reconnect() {

    unsigned long now = millis();
    if (now - _lastReconnectAttempt < _reconnectInterval) {
        return;
    }

    _lastReconnectAttempt = now;
    _reconnectAttempts++;

    Serial.print("[WiFi] Reconnect attempt ");
    Serial.print(_reconnectAttempts);

    if (_reconnectAttempts > _maxReconnectAttempts) {
        Serial.println("[WiFi] Max attempts reached. Still retrying...");
        _reconnectAttempts = 0;  
    }

    WiFi.disconnect(true);
    vTaskDelay(500 / portTICK_PERIOD_MS); 

    connect();
}


bool WifiManager::isConnected() {
    return WiFi.status() == WL_CONNECTED;
}


void WifiManager::maintainConnection() {
    if (!isConnected()) {
        reconnect();
    }
}


String WifiManager::getIPAddress() {
    return WiFi.localIP().toString();
}


String WifiManager::getMacAddress() {
    return WiFi.macAddress();
}


int WifiManager::getSignalStrength() {
    return WiFi.RSSI();
}