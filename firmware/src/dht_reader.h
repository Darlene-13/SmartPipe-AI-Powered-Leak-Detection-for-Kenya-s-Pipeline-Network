#pragma once


#include <DHT.h>
#include "config.h"
#include <Arduino.h>

class DHTReader {
    public:
        DHTReader();
        void initDHT();
        bool readSensor(float& temperature, float& humidity);

    private:

        DHT _dht; // do not initialize here, as the pin and type are needed from config.h


};