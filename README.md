# Pipeline leak detection

This repository contains an end-to-end leak and blockage detection system for instrumented pipelines. Sensor readings travel from an ESP32 device to the backend, are scored by the ML service, and appear in a small operations dashboard. The system can also replay recorded scenarios, which makes it useful for demos and development without live hardware.

## Project map

The top-level folders are organized around the parts of the product you will actually work on:

| Area | What it does | Start here |
| --- | --- | --- |
| `firmware/` | ESP32 firmware, sensor simulation, LEDs/buzzer/display, and MQTT publishing | `firmware/src/main.cpp` |
| `LeakDetectionApplication/` | Spring Boot API, authentication, persistence, MQTT ingestion, alerts, analytics, and WebSockets | `LeakDetectionApplication/src/main/java/io/github/darlene/leakdetectionapplication/` |
| `ml_service/` | Python prediction service, preprocessing, replay support, notebooks, and model evaluation assets | `ml_service/src/app.py` |
| `dashboard/` | React/Vite operator dashboard for live readings, history, alerts, and recommendations | `dashboard/src/App.tsx` |
| `tools/` | One-off operational helpers such as database seeding and JWT secret generation | `tools/` |

The backend is organised by feature. Each feature folder keeps its controllers, services, domain models, DTOs, repositories, and feature-specific errors together, so a change to authentication or sensor ingestion stays in one place. Cross-cutting configuration and shared error handling live in `configuration/` and `shared/`.

## How the pieces fit together

```text
ESP32 / replay data --MQTT--> Spring Boot API --HTTP--> ML service
                                      |                  |
                                      +-- PostgreSQL      +-- prediction
                                      +-- Redis cache
                                      +-- WebSocket --> React dashboard
```

## Prerequisites

- Java 21 and Maven (the backend includes `mvnw`)
- Node.js 20 and npm
- Python 3.10+ for the ML service and tools
- PostgreSQL, Redis, and an MQTT broker

Copy `.env.example` to `.env` and fill in the services you plan to run. Keep secrets out of git. `JWT_SECRET` should be a long, random value; `tools/JwtSecretGenerator.java` can generate one.

## Run locally

Start each component in its own terminal:

```bash
# Backend API
cd LeakDetectionApplication
./mvnw spring-boot:run

# ML service
cd ml_service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python -m src.app

# Dashboard
cd dashboard
npm ci
npm run dev
```

For a hardware-free demo, run the replay/simulation endpoints from the dashboard or use the firmware data under `firmware/src/data/`. The backend's OpenAPI UI is available at `/swagger-ui.html` when the API is running.

## Useful checks

```bash
cd LeakDetectionApplication && ./mvnw test
cd dashboard && npm run build
```

The ML notebooks live in `ml_service/notebooks/live/`. Their charts and evaluation outputs are kept under `ml_service/assets/` so experiments remain separate from the serving code.

## Configuration notes

The application reads environment-specific Spring configuration from `LeakDetectionApplication/src/main/resources/`. Database schema migrations are in `.../resources/db/migration/`. MQTT and ML connectivity are configured through the variables in `.env.example`.

## License

See [LICENSE](LICENSE).


### WRITTEN BY:
Darlene Wendy