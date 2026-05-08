#pragma once

#include <WiFi.h>
#include <Preferences.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "config.h"

class WifiManager {

    public:
        WifiManager(const char* ssid, const char* password);

        //Core connection
        bool connect();
        void disconnect();
        void reconnect();
        bool isConnected();

        // Monitoring
        void maintainConnection();
        bool hasInternetConnection();

        // Info
        String getIPAddress();
        String getMacAddress();
        int getSignalStrength();

        //Configuration
        void setCredentials(const char* ssid, const char* password);
        void saveCredentials();
        void loadCredentials();

        // Access Point mode
        void startAccessPoint();
        void stopAccessPoint();

        void resetWiFi();

    private:

        String _ssid;
        String _password;

        unsigned long _lastReconnectAttempt;
        int _reconnectAttempts;

        const unsigned long _reconnectInterval = 5000; // 5 seconds
        const int _maxReconnectAttempts =5;

        bool _apMode;

        Preferences _prefs;
        bool _credentialsLoaded;

};