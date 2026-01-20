# Hydraulic Modeling and Instrumentation for Pipeline Leak Detection

### Overview of The Project
Industrial pipeline systems are critical for transporting liquids and slurries in sectors such as mining, mineral processing, petroleum, and water distribution. Ensuring their reliable and safe operation requires accurate monitoring, rapid fault detection, and timely operational response. Traditional pipeline monitoring relies on point sensors integrated with SCADA systems, which provide centralized data collection and alarms but are limited by threshold-based logic, delayed detection, and high alarm density. Advanced sensing methods, such as distributed fiber-optic systems, have improved monitoring coverage but generate large volumes of complex data, creating engineering challenges for interpretation and decision-making.

This project presents an engineering-driven framework that integrates hydraulic modeling, instrumentation design, and a machine learning-assisted decision-support platform for pipeline fault detection and operational guidance. A dynamic hydraulic model simulates normal operating conditions and representative fault scenarios, including leaks, mechanical failures, corrosion-induced defects, and operational transients. Instrumentation strategies, incorporating pressure and flow sensors, fiber-optic distributed sensing, and isolation valves, are designed based on hydraulic principles to optimize detection sensitivity and event localization.

The framework applies supervised machine learning techniques to classify events and assess severity, providing actionable recommendations to support timely operational decisions without replacing existing SCADA infrastructure. A petroleum pipeline network, representative of large-scale industrial transmission systems, is used as a reference case study to demonstrate system performance. Simulation results show that the integrated approach reduces decision-making time, improves fault identification accuracy, and provides a systematic methodology for combining hydraulic engineering with digital decision-support tools. The study highlights the potential of engineering-based, simulation-supported platforms to enhance pipeline integrity, operational safety, and process efficiency across industrial applications.


## ARCHITECTURE
````
┌────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                           │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐ │
│  │   Web Browser    │  │  Mobile Browser  │  │  Admin Portal │ │
│  │   (React/Vue)    │  │   (Responsive)   │  │               │ │
│  └──────────────────┘  └──────────────────┘  └───────────────┘ │
└────────────────────────────┬───────────────────────────────────┘
                        HTTPS / WSS
┌────────────────────────────▼────────────────────────────────────┐
│                      API GATEWAY                                │
│                  (Spring Cloud Gateway / Nginx)                 │
│               - Routing, Load Balancing, Rate Limiting          │
└────────────────────────────┬────────────────────────────────────┘
                             │
              ┌──────────────┴──────────────┐
              │                             │
┌─────────────▼──────────────┐  ┌──────────▼───────────────────────┐
│   SPRING BOOT BACKEND      │  │   PYTHON ML SERVICE              │
│   (Core Application)       │  │   (Intelligence Engine)          │
│                            │  │                                  │
│  ┌──────────────────────┐  │  │  ┌────────────────────────────┐  │
│  │  API Controllers     │  │  │  │  Flask/FastAPI REST API    │  │ 
│  │  - Events            │  │  │  │  - /classify-leak          │  │
│  │  - Sensors           │  │  │  │  - /assess-severity        │  │
│  │  - Recommendations   │◄─┼──┼──┤  - /recommend-action       │  │ 
│  │  - Users             │  │  │  │  - /model-metrics          │  │
│  │  - Dashboard         │  │  │  └────────────────────────────┘  │
│  └──────────────────────┘  │  │                                  │
│                            │  │  ┌────────────────────────────┐  │
│  ┌──────────────────────┐  │  │  │  ML Models                 │  │
│  │  Business Logic      │  │  │  │  - Leak Classifier         │  │
│  │  - Event Processing  │  │  │  │  - Severity Assessor       │  │
│  │  - Alert Management  │  │  │  │  - Response Recommender    │  │
│  │  - Response Mapping  │  │  │  └────────────────────────────┘  │
│  └──────────────────────┘  │  │                                  │
│                            │  │  ┌────────────────────────────┐  │
│  ┌──────────────────────┐  │  │  │  Feature Engineering       │  │
│  │  Data Access Layer   │  │  │  │  - Signal Processing       │  │
│  │  (Spring Data JPA)   │  │  │  │  - Statistical Features    │  │
│  └──────────────────────┘  │  │  │  - Domain Features         │  │
│                            │  │  └────────────────────────────┘  │
│  ┌──────────────────────┐  │  │                                  │
│  │  Security            │  │  │  ┌────────────────────────────┐  │
│  │  (Spring Security)   │  │  │  │  Model Management          │  │
│  │  - JWT Auth          │  │  │  │  - Versioning              │  │
│  │  - RBAC              │  │  │  │  - A/B Testing             │  │
│  └──────────────────────┘  │  │  │  - Performance Tracking    │  │
│                            │  │  └────────────────────────────┘  │
└────────────┬───────────────┘  └─────────────┬────────────────────┘
             │                                │
             │                                │
┌────────────▼────────────────────────────────▼────────────────────┐
│                        DATA LAYER                                │
│  ┌──────────────────────┐  ┌──────────────────────────────────┐  │
│  │  Relational DB       │  │  Time-Series DB        │  │
│  │  (PostgreSQL)        │  │  (InfluxDB / TimescaleDB)        │  │
│  │                      │  │                                  │  │
│  │  - events            │  │  - sensor_readings (raw)         │  │
│  │  - users             │  │  - pressure_data                 │  │ 
│  │  - recommendations   │  │  - flow_data                     │  │
│  │  - pipeline_config   │  │  - temperature_data              │  │
│  │  - audit_logs        │  │  - fiber_optic_signals           │  │
│  └──────────────────────┘  └──────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────┐  ┌──────────────────────────────────┐  │
│  │  Cache (Redis)       │  │  File Storage (MinIO/S3)         │  │
│  │  - Session data      │  │  - ML models                     │  │
│  │  - Frequent queries  │  │  - Training data                 │  │
│  │  - Real-time metrics │  │  - Reports/exports               │  │
│  └──────────────────────┘  └──────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
│
┌────────────────────────────▼────────────────────────────────────┐
│                  SIMULATION/TESTING LAYER                       │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Pipeline Simulator (Python)                             │   │
│  │  - Generates realistic sensor data                       │   │
│  │  - Simulates normal operations and failure scenarios     │   │
│  │  - Sends data to backend via REST API or Kafka           │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘

````

## Key Technologies
### FRONTEND - TECHNOLOGIES

### BACKEND - TECHNOLOGIES
1. Springboot

### MACHINE LEARNING
1. Matlab Simulink
2. Python for Machine Learning




## Written By:
Darlene Wendy