#include <Arduino.h>
#include "config.h"
#include "dht_reader.h"

DHTReader::DHTReader() : _dht(PIN_DHT, DHT_TYPE) {}

void DHTReader::initDHT(){
    _dht.begin();
}

bool DHTReader::readSensor(float& temperature, float& humidity){
    temperature = _dht.readTemperature();
    humidity = _dht.readHumidity();
    return !isnan(temperature) && !isnan(humidity);
}