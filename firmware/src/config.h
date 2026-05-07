#pragma once

//WIFI
#define WIFI_SSID "WENDIE"
#define WIFI_PASSWORD "#Darlene1"
#define WIFI_TIMEOUT_MS 10000

// MQTT
#define MQTT_BROKER 
#define MQTT_PORT
#define MQTT_CLIENT_ID
#define MQTT_USERNAME
#define MQTT PASSWORD 



// MQTT Topics
#define TOPIC_SENSOR_DATA "pipeline/sensors/node"
#define TOPIC_LED_STATUS "pipeline/led/status"
#define TOPIC_HEARTBEAT "pipeline/heartbeat"


//TIMING
#define PUBLISH_INTERVAL_MS 500
#define DISPLAY_INTERVAL_MS 1000
#define HEARTBEAT_INTERVAL_MS = 30000
#define DHT_READ_INTERVAL_MS = 2000

//PINS
#define PIN_DHT 4
#define PIN_OLED_SDA 21
#define PIN_POT 34
#define PIN_LED_RED 25
#define PIN_LED_GREEN 26
#define PIN_LED_BLUE 27
#define PIN_BUZZER 32
#define PIN_BUTON 33


//OLED 
#define OLED_WIDTH 128
#define OLED_HEIGHT 64
#define OLED_ADDRESS 0X3C


// Data replay
#define TOTAL_TIMESTEPS 700
#define NODES_PER_TIMESTEP 146
#define SENSOR_NODE_A_IDX 10   
#define SENSOR_NODE_B_IDX 73 

//NOISE
#define NOISE_SCALE_FACTOR 0.02

//STACK SIZES
#define STACK_PUBLISH 8192
#define STACK_MQTT 4096
#define STACK_DISPLAY 4096
#define STACK_SENSOR 2048


//Prioties
#define PRIORITY_HIGH 2
#define PRIORITY_LOW 1