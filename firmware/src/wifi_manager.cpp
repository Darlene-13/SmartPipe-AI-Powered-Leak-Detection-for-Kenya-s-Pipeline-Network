#include <Arduino.h>
#include "wifi_manager.h"
#include <WiFiClient.h>

WifiManager::WifiManager(const char* ssid, const char* password){

    _lastReconnectAttempt = 0;
    _reconnectAttempts = 0;
    _apMode = false;
    _credentialsLoaded = false;

    loadCredentials();

    if (!_credentialsLoaded){
        _ssid = ssid;
        _password = password;
    }
}

bool WifiManager::connect(){

    if(_ssid == "") return false;

    WiFi.mode(WIFI_STA);
    WiFi.begin(_ssid.c_str(), _password.c_str());

    int timeout = 20;

    while (WiFi.status() != WL_CONNECTED && timeout > 0){
        delay(500);
        timeout --;
    }

    if (WiFi.status() == WL_CONNECTED){
        _reconnectAttempts = 0;

        saveCredentials();
        return true;
    }

    return false;
}


void WifiManager::disconnect(){
    WiFi.disconnect();
}


void WifiManager::reconnect(){
    if(_apMode) return;

    unsigned long now = millis();

    if(now - _lastReconnectAttempt < _reconnectInterval){
        return;
    }

    _lastReconnectAttempt = now;

    if(_reconnectAttempts >= _maxReconnectAttempts){
        startAccessPoint();  //fallback
        return;
    }

    _reconnectAttempts++;

    WiFi.disconnect(true);   // Full clear the previous state.
    delay(500);

    connect();

}

bool WifiManager::isConnected(){
    return WiFi.status() == WL_CONNECTED;
}

void WifiManager::maintainConnection(){

    if(!isConnected()){
        reconnect();
    }

}

bool WifiManager::hasInternetConnection(){
    if(!isConnected()) return false;

    WiFiClient client;

    return client.connect("8.8.8.8", 53);
}


String WifiManager::getIPAddress(){
    return WiFi.localIP().toString();
}

String WifiManager::getMacAddress(){
    return WiFi.macAddress();
}

int WifiManager::getSignalStrength(){
    return WiFi.RSSI();
}

void WifiManager::setCredentials(const char* ssid, const char* password){

    // Save only when credentials are changed.
    if(_ssid != ssid || _password != password){
        _ssid = ssid;
        _password = password;

        saveCredentials();
    }
}

void WifiManager::saveCredentials(){

    _prefs.begin("wifi", false);

    _prefs.putString("ssid", _ssid);
    _prefs.putString("pass", _password);

    _prefs.end();
}

void WifiManager::loadCredentials(){

    _prefs.begin("wifi", true);

    _ssid = _prefs.getString("ssid", "");
    _password = _prefs.getString("pass", "");

    _prefs.end();

    _credentialsLoaded = (_ssid != "");
}

void WifiManager::startAccessPoint(){
    _apMode = true;

    WiFi.disconnect();
    WiFi.mode(WIFI_AP);

    WiFi.softAP("ESP32_SETUP_WIFI", "12345678");

    _reconnectAttempts = 0;
}

void WifiManager::stopAccessPoint(){

    WiFi.softAPdisconnect(true);

    _apMode =false;

    WiFi.mode(WIFI_STA);

    connect();
}

void WifiManager::resetWiFi(){

    WiFi.disconnect(true);
    delay(1000);

    WiFi.mode(WIFI_OFF);
    delay(500);

    WiFi.mode(WIFI_STA);

    _reconnectAttempts =0;

    connect();
}

