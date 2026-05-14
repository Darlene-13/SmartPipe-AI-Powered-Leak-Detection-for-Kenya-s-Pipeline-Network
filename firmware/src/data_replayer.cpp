#include "data_replayer.h"
#include <string.h>

DataReplayer::DataReplayer()
    : _currentTimestep(0),
      _activeData(NORMAL_1_DATA),
      _activeLen(NORMAL_1_LEN) {
    strncpy(_currentScenario, "normal_1", sizeof(_currentScenario));
}

void DataReplayer::initReplayer() {
    setScenario("normal_1");
}

void DataReplayer::setScenario(const char* scenarioName) {
    strncpy(_currentScenario, scenarioName, sizeof(_currentScenario));
    _currentTimestep = 0;

    if      (strcmp(scenarioName, "normal_1")      == 0) { _activeData = NORMAL_1_DATA;      _activeLen = NORMAL_1_LEN; }
    else if (strcmp(scenarioName, "normal_2")      == 0) { _activeData = NORMAL_2_DATA;      _activeLen = NORMAL_2_LEN; }
    else if (strcmp(scenarioName, "normal_3")      == 0) { _activeData = NORMAL_3_DATA;      _activeLen = NORMAL_3_LEN; }
    else if (strcmp(scenarioName, "leak_incipient")== 0) { _activeData = LEAK_INCIPIENT_DATA; _activeLen = LEAK_INCIPIENT_LEN; }
    else if (strcmp(scenarioName, "leak_moderate") == 0) { _activeData = LEAK_MODERATE_DATA;  _activeLen = LEAK_MODERATE_LEN; }
    else if (strcmp(scenarioName, "leak_critical") == 0) { _activeData = LEAK_CRITICAL_DATA;  _activeLen = LEAK_CRITICAL_LEN; }
    else if (strcmp(scenarioName, "blockage_25")   == 0) { _activeData = BLOCKAGE_25_DATA;    _activeLen = BLOCKAGE_25_LEN; }
    else if (strcmp(scenarioName, "blockage_50")   == 0) { _activeData = BLOCKAGE_50_DATA;    _activeLen = BLOCKAGE_50_LEN; }
    else if (strcmp(scenarioName, "blockage_75")   == 0) { _activeData = BLOCKAGE_75_DATA;    _activeLen = BLOCKAGE_75_LEN; }
    else {
        // unknown scenario — fall back to normal_1
        _activeData = NORMAL_1_DATA;
        _activeLen  = NORMAL_1_LEN;
        strncpy(_currentScenario, "normal_1", sizeof(_currentScenario));
    }
}

SensorReading DataReplayer::getNextReading() {
    SensorReading reading;

    // read current row from PROGMEM
    reading.nodeAPressure = pgm_read_float(&_activeData[_currentTimestep][0]);
    reading.velocityA     = pgm_read_float(&_activeData[_currentTimestep][1]);
    reading.nodeBPressure = pgm_read_float(&_activeData[_currentTimestep][2]);
    reading.velocityB     = pgm_read_float(&_activeData[_currentTimestep][3]);
    reading.nodeCPressure = pgm_read_float(&_activeData[_currentTimestep][4]);
    reading.velocityC     = pgm_read_float(&_activeData[_currentTimestep][5]);

    // metadata
    reading.currentTimestep = _currentTimestep;
    strncpy(reading.currentScenario, _currentScenario, sizeof(reading.currentScenario));

    // advance — wrap at end of scenario
    _currentTimestep++;
    if (_currentTimestep >= _activeLen) {
        _currentTimestep = 0;
    }

    return reading;
}