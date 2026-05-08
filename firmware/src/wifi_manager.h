#pragma once

#include <WiFi.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "config.h"

class WifiManager {

    public:
        WifiManager(const char* ssid, const char* password);

        // Core connection
        bool connect();
        void disconnect();
        void reconnect();
        bool isConnected();
        void maintainConnection();

        String getIPAddress();
        String getMacAddress();
        int    getSignalStrength();

    private:
        const char* _ssid;
        const char* _password;

        unsigned long _lastReconnectAttempt;
        int           _reconnectAttempts;

        const unsigned long _reconnectInterval  = 5000;

        const int _maxReconnectAttempts = 10;
};