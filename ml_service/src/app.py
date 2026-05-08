import os
import logging
import traceback
import threading
import json
import time
import psycopg2
import paho.mqtt.client as mqtt
from datetime    import datetime, timezone
from flask       import Flask, request, jsonify
from flask_cors  import CORS
from models      import registry
from predictor   import predictor

logging.basicConfig(
    level  = logging.INFO,
    format = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

app = Flask(__name__)
CORS(app)

# Load model registry at startup
try:
    registry.load()
    logging.info("Model registry loaded successfully")
except Exception as e:
    logging.error(f"FATAL: Could not load model: {e}")
    raise

@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status"       : "ok",
        "active_model" : registry.active_name,
        "window_size"  : registry.window_size,
        "n_features"   : registry.n_features,
        "buffer_status": predictor.get_buffer_status()
    }), 200

@app.route("/predict", methods=["POST"])
def predict_live():
    try:
        raw = request.get_json()
        if not raw:
            return jsonify({"error": "No JSON body received"}), 400

        device_id = raw.get("device_id", "default")

        required = [
            "node_a_pressure", "velocity_a",
            "node_b_pressure", "velocity_b",
            "node_c_pressure", "velocity_c"
        ]
        missing = [f for f in required if f not in raw]
        if missing:
            return jsonify({
                "error"  : "Missing required fields",
                "missing": missing
            }), 400

        result = predictor.predict(device_id, raw)
        return jsonify(result), 200

    except Exception as e:
        logging.error(f"Prediction error: {traceback.format_exc()}")
        return jsonify({
            "error" : "Prediction failed",
            "detail": str(e)
        }), 500

@app.route("/reset/<device_id>", methods=["POST"])
def reset_buffer(device_id):
    predictor.reset_buffer(device_id)
    return jsonify({
        "status"   : "ok",
        "device_id": device_id,
        "message"  : f"Buffer reset for {device_id}"
    }), 200

@app.route("/status", methods=["GET"])
def status():
    return jsonify(predictor.get_buffer_status()), 200


# ── MQTT Replay Thread ─────────────────────────────────────────
def start_replay_thread():
    """
    Reads sensor_readings from TimescaleDB in order and publishes
    each row to MQTT so Spring Boot processes them as live readings.

    Flow:
        TimescaleDB -> this thread -> MQTT -> MqttSubscriber (Spring Boot)
        -> ProcessingService -> ML prediction -> WebSocket -> Dashboard

    Restarts from row 1 when all rows published.
    Only starts if MQTT_BROKER_URL env var is set.
    """
    broker_url = os.environ.get("MQTT_BROKER_URL")
    if not broker_url:
        logging.info("MQTT_BROKER_URL not set - replay simulator disabled")
        return

    db_url    = os.environ.get("SEED_DB_URL") or os.environ.get("DB_URL")
    username  = os.environ.get("MQTT_USERNAME")
    password  = os.environ.get("MQTT_PASSWORD")
    topic     = os.environ.get("MQTT_TOPIC",       "pipeline/sensors/ESP32_REPLAY_01/node")
    interval  = float(os.environ.get("REPLAY_INTERVAL_MS", "2000")) / 1000.0
    device_id = os.environ.get("REPLAY_DEVICE_ID", "ESP32_REPLAY_01")

    if not db_url:
        logging.warning("No DB URL found - replay simulator skipping")
        return

    def replay():
        # Connect to DB
        try:
            conn = psycopg2.connect(db_url)
            logging.info("Replay: DB connected")
        except Exception as e:
            logging.error(f"Replay: DB connection failed: {e}")
            return

        # Connect to MQTT
        client = mqtt.Client(
            client_id=f"replay-{int(time.time())}",
            protocol=mqtt.MQTTv5
        )
        client.username_pw_set(username, password)

        url = broker_url
        if url.startswith("ssl://"):
            host, port = url[6:].rsplit(":", 1)
            client.tls_set()
        else:
            host, port = url.replace("tcp://", "").rsplit(":", 1)

        try:
            client.connect(host, int(port), keepalive=60)
            client.loop_start()
            logging.info(f"Replay: MQTT connected to {host}:{port}")
        except Exception as e:
            logging.error(f"Replay: MQTT connection failed: {e}")
            return

        time.sleep(3)

        # Load all IDs once into memory
        try:
            with conn.cursor() as cur:
                cur.execute("""
                    SELECT id FROM sensor_readings
                    ORDER BY reading_time ASC
                """)
                ids = [row[0] for row in cur.fetchall()]
            logging.info(f"Replay: {len(ids):,} readings loaded - starting")
        except Exception as e:
            logging.error(f"Replay: failed to load IDs: {e}")
            return

        idx   = 0
        cycle = 0
        total = len(ids)

        while True:
            try:
                if idx >= total:
                    idx    = 0
                    cycle += 1
                    logging.info(f"Replay: cycle {cycle} - restarting from row 1")

                with conn.cursor() as cur:
                    cur.execute("""
                        SELECT node_a_pressure, velocity_a,
                               node_b_pressure, velocity_b,
                               node_c_pressure, velocity_c,
                               scenario
                        FROM sensor_readings
                        WHERE id = %s
                    """, (ids[idx],))
                    row = cur.fetchone()

                if row:
                    payload = json.dumps({
                        "device_id":       device_id,
                        "ts":              datetime.now(timezone.utc).isoformat(),
                        "node_a_pressure": row[0],
                        "velocity_a":      row[1],
                        "node_b_pressure": row[2],
                        "velocity_b":      row[3],
                        "node_c_pressure": row[4],
                        "velocity_c":      row[5],
                        "sc":              row[6] or "replay",
                    })
                    client.publish(topic, payload, qos=1)

                    if idx % 100 == 0:
                        logging.info(
                            f"Replay: [{idx+1}/{total}] cycle={cycle} "
                            f"pA={row[0]:.0f} pB={row[2]:.0f} pC={row[4]:.0f}"
                        )

                idx += 1
                time.sleep(interval)

            except Exception as e:
                logging.error(f"Replay error at idx {idx}: {e}")
                time.sleep(5)

    t = threading.Thread(target=replay, name="ReplaySimulator", daemon=True)
    t.start()
    logging.info("Replay simulator thread started")


if __name__ == "__main__":
    # Local: python app.py
    port = int(os.environ.get("PORT", 5000))
    start_replay_thread()
    app.run(host="0.0.0.0", port=port, debug=False, threaded=True)
else:
    # Render: gunicorn app:app
    # __name__ is "app" not "__main__" so we start the thread here
    start_replay_thread()