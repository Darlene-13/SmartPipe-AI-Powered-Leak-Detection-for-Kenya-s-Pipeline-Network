# AI-POWERED LEAK DETECTION SYSTEM


### PROJECT STRUCTURE
```aiignore
ai-pipeline-leak-detection/
│
├── .github/
│   └── workflows/
│       └── build.yml
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/aidetect/
│   │   │   │   │
│   │   │   │   ├── LeakDetectionApplication.java
│   │   │   │   │
│   │   │   │   ├── config/
│   │   │   │   │   ├── MqttConfig.java
│   │   │   │   │   ├── WebSocketConfig.java
│   │   │   │   │   ├── RestClientConfig.java
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   └── OpenApiConfig.java
│   │   │   │   │
│   │   │   │   ├── domain/
│   │   │   │   │   ├── SensorReading.java
│   │   │   │   │   ├── FaultAlert.java
│   │   │   │   │   ├── FaultClass.java
│   │   │   │   │   ├── SeverityLevel.java
│   │   │   │   │   └── SystemStatus.java
│   │   │   │   │
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/
│   │   │   │   │   │   ├── SensorReadingRequest.java
│   │   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   │   └── SimulationRequest.java
│   │   │   │   │   │
│   │   │   │   │   └── response/
│   │   │   │   │       ├── SensorReadingResponse.java
│   │   │   │   │       ├── FaultAlertResponse.java
│   │   │   │   │       ├── SystemStatusResponse.java
│   │   │   │   │       ├── AnalyticsSummaryResponse.java
│   │   │   │   │       ├── LatencyStatsResponse.java
│   │   │   │   │       ├── LoginResponse.java
│   │   │   │   │       └── MLPredictionResponse.java
│   │   │   │   │
│   │   │   │   ├── repository/
│   │   │   │   │   ├── SensorReadingRepository.java
│   │   │   │   │   └── FaultAlertRepository.java
│   │   │   │   │
│   │   │   │   ├── service/
│   │   │   │   │   ├── ProcessingService.java
│   │   │   │   │   ├── FeatureExtractionService.java
│   │   │   │   │   ├── MLBridgeService.java
│   │   │   │   │   ├── RecommendationService.java
│   │   │   │   │   ├── AlertService.java
│   │   │   │   │   ├── LatencyTrackingService.java
│   │   │   │   │   └── AuthService.java
│   │   │   │   │
│   │   │   │   ├── mqtt/
│   │   │   │   │   ├── MqttSubscriber.java
│   │   │   │   │   └── MqttPublisher.java
│   │   │   │   │
│   │   │   │   ├── api/
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── SensorController.java
│   │   │   │   │   ├── AlertController.java
│   │   │   │   │   ├── StatusController.java
│   │   │   │   │   ├── AnalyticsController.java
│   │   │   │   │   └── SimulationController.java
│   │   │   │   │
│   │   │   │   ├── websocket/
│   │   │   │   │   └── AlertWebSocketHandler.java
│   │   │   │   │
│   │   │   │   ├── security/
│   │   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   │   ├── JwtAuthFilter.java
│   │   │   │   │   └── OperatorDetails.java
│   │   │   │   │
│   │   │   │   ├── mapper/
│   │   │   │   │   ├── SensorReadingMapper.java
│   │   │   │   │   └── FaultAlertMapper.java
│   │   │   │   │
│   │   │   │   └── exception/
│   │   │   │       ├── GlobalExceptionHandler.java
│   │   │   │       ├── MLServiceUnavailableException.java
│   │   │   │       ├── InvalidSensorDataException.java
│   │   │   │       └── ScenarioNotFoundException.java
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-prod.yml
│   │   │       └── db/
│   │   │           └── migration/
│   │   │               ├── V1__create_sensor_readings.sql
│   │   │               └── V2__create_fault_alerts.sql
│   │   │
│   │   └── test/
│   │       └── java/com/aidetect/
│   │           ├── service/
│   │           │   ├── ProcessingServiceTest.java
│   │           │   ├── FeatureExtractionServiceTest.java
│   │           │   └── MLBridgeServiceTest.java
│   │           │
│   │           └── api/
│   │               ├── SensorControllerTest.java
│   │               └── AlertControllerTest.java
│   │
│   ├── pom.xml
│   └── Dockerfile
│
│
├── ml-service/
│   ├── app/
│   │   ├── __init__.py
│   │   ├── routes.py
│   │   ├── predictor.py
│   │   ├── preprocessor.py
│   │   └── schemas.py
│   │
│   ├── training/
│   │   ├── train_random_forest.py
│   │   ├── train_xgboost.py
│   │   ├── train_svm.py
│   │   ├── train_lstm.py
│   │   ├── evaluate_models.py
│   │   └── feature_engineering.py
│   │
│   ├── data/
│   │   ├── raw/
│   │   │   └── .gitkeep
│   │   ├── processed/
│   │   │   └── .gitkeep
│   │   └── .gitignore
│   │
│   ├── model/
│   │   └── .gitkeep
│   │
│   ├── notebooks/
│   │   ├── 01_data_exploration.ipynb
│   │   ├── 02_feature_engineering.ipynb
│   │   ├── 03_model_training.ipynb
│   │   └── 04_model_evaluation.ipynb
│   │
│   ├── tests/
│   │   ├── test_routes.py
│   │   ├── test_predictor.py
│   │   └── test_preprocessor.py
│   │
│   ├── app.py
│   ├── requirements.txt
│   ├── Dockerfile
│   └── README.md
│
│
├── firmware/
│   ├── src/
│   │   ├── main.cpp
│   │   ├── mqtt_handler.cpp
│   │   ├── data_replayer.cpp
│   │   ├── noise_injector.cpp
│   │   ├── display_handler.cpp
│   │   ├── led_controller.cpp
│   │   └── buzzer_handler.cpp
│   │
│   ├── include/
│   │   ├── config.example.h
│   │   ├── mqtt_handler.h
│   │   ├── data_replayer.h
│   │   ├── noise_injector.h
│   │   ├── display_handler.h
│   │   ├── led_controller.h
│   │   └── buzzer_handler.h
│   │
│   ├── data/
│   │   └── scenarios/
│   │       ├── normal_baseline.h
│   │       ├── leak_incipient.h
│   │       ├── leak_moderate.h
│   │       ├── leak_critical.h
│   │       ├── blockage_25.h
│   │       ├── blockage_50.h
│   │       └── blockage_75.h
│   │
│   ├── lib/
│   │   └── .gitkeep
│   │
│   └── platformio.ini
│
│
├── dashboard/
│   ├── src/
│   │   ├── pages/
│   │   │   ├── Login.jsx
│   │   │   ├── Dashboard.jsx
│   │   │   ├── History.jsx
│   │   │   └── Simulation.jsx
│   │   │
│   │   ├── components/
│   │   │   ├── PressureChart.jsx
│   │   │   ├── NodeStatusCard.jsx
│   │   │   ├── AlertsTable.jsx
│   │   │   ├── SystemStatus.jsx
│   │   │   ├── AIRecommendation.jsx
│   │   │   └── LatencyDisplay.jsx
│   │   │
│   │   ├── hooks/
│   │   │   ├── useWebSocket.js
│   │   │   └── useAuth.js
│   │   │
│   │   ├── services/
│   │   │   └── api.js
│   │   │
│   │   └── App.jsx
│   │
│   ├── package.json
│   └── Dockerfile
│
│
├── docs/
│   ├── architecture.png
│   ├── api-reference.md
│   ├── setup-guide.md
│   └── ansys-methodology.md
│
│
├── docker-compose.yml
├── docker-compose.dev.yml
├── .env.example
├── .gitignore
└── README.md
```



#### Written By:
Darlene Wendy