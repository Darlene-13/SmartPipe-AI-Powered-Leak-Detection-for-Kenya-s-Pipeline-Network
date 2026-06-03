"""
replay_simulator.py — Replays seeded sensor data through MQTT

Flow:
    TimescaleDB (seeded rows)
        -> this script reads rows in order
        -> publishes to MQTT topic: pipeline/sensors/node
        -> MqttSubscriber (Spring Boot) receives it
        -> ProcessingService -> ML prediction -> WebSocket
        -> Dashboard shows live flowing data

When real ESP32 is publishing:
    - Script detects recent MQTT activity on the topic
    - Backs off automatically (ESP32 takes priority)
    - Resumes when ESP32 goes quiet

Usage:
    pip install paho-mqtt psycopg2-binary python-dotenv
    python replay_simulator.py

Config via .env:
    SEED_DB_URL      - TimescaleDB connection
    MQTT_BROKER_URL  - e.g. ssl://xxx.hivemq.cloud:8883
    MQTT_USERNAME
    MQTT_PASSWORD
    REPLAY_INTERVAL_MS  - ms between publishes (default 2000)
    REPLAY_DEVICE_ID    - device_id to use (default ESP32_REPLAY_01)
"""

import os
import json
import time
import logging
import psycopg2
import paho.mqtt.client as mqtt
from datetime import datetime, timezone
from dotenv import load_dotenv

load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s"
)
log = logging.getLogger("replay")

DB_URL           = os.environ["SEED_DB_URL"]
MQTT_BROKER_URL  = os.environ["MQTT_BROKER_URL"]   # ssl://host:port
MQTT_USERNAME    = os.environ["MQTT_USERNAME"]
MQTT_PASSWORD    = os.environ["MQTT_PASSWORD"]
MQTT_TOPIC       = os.environ.get("MQTT_TOPIC", "pipeline/sensors/node")
INTERVAL_MS      = int(os.environ.get("REPLAY_INTERVAL_MS", "2000"))
DEVICE_ID        = os.environ.get("REPLAY_DEVICE_ID", "ESP32_REPLAY_01")
BATCH_SIZE       = 100
ESP32_TIMEOUT_S  = 15

last_esp32_message = 0.0

def on_connect(client, userdata, flags, rc, properties=None):
    if rc == 0:
        log.info("Connected to MQTT broker")
        client.subscribe(MQTT_TOPIC)
    else:
        log.error(f"MQTT connect failed: rc={rc}")

def on_message(client, userdata, msg):
    global last_esp32_message
    try:
        payload = json.loads(msg.payload.decode())
        device  = payload.get("device_id", "")
        # Only count real ESP32 messages, not our own replays
        if device and not device.startswith("ESP32_REPLAY"):
            last_esp32_message = time.time()
            log.debug(f"Real ESP32 active: {device}")
    except Exception:
        pass

def build_mqtt_client() -> mqtt.Client:
    client = mqtt.Client(
        client_id=f"replay-{int(time.time())}",
        protocol=mqtt.MQTTv5
    )
    client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)

    url = MQTT_BROKER_URL
    if url.startswith("ssl://"):
        host, port = url[6:].rsplit(":", 1)
        client.tls_set()
    elif url.startswith("tcp://"):
        host, port = url[6:].rsplit(":", 1)
    else:
        host, port = url.rsplit(":", 1)

    client.on_connect = on_connect
    client.on_message = on_message
    client.connect(host, int(port), keepalive=60)
    client.loop_start()
    return client

def fetch_all_reading_ids(conn) -> list[int]:
    """Fetch all sensor reading IDs ordered by reading_time."""
    with conn.cursor() as cur:
        cur.execute("""
            SELECT id FROM sensor_readings
            ORDER BY reading_time ASC
        """)
        return [row[0] for row in cur.fetchall()]

def fetch_row(conn, reading_id: int) -> dict | None:
    """Fetch one sensor reading by ID."""
    with conn.cursor() as cur:
        cur.execute("""
            SELECT device_id, reading_time,
                   node_a_pressure, velocity_a,
                   node_b_pressure, velocity_b,
                   node_c_pressure, velocity_c,
                   dp_dt_a, dp_dt_b, dp_dt_c,
                   scenario
            FROM sensor_readings
            WHERE id = %s
        """, (reading_id,))
        row = cur.fetchone()
        if not row:
            return None
        return {
            "device_id":       DEVICE_ID,
            "ts":              datetime.now(timezone.utc).isoformat(),
            "node_a_pressure": row[2],
            "velocity_a":      row[3],
            "node_b_pressure": row[4],
            "velocity_b":      row[5],
            "node_c_pressure": row[6],
            "velocity_c":      row[7],
            "dp_dt_a":         row[8] or 0.0,
            "dp_dt_b":         row[9] or 0.0,
            "dp_dt_c":         row[10] or 0.0,
            "sc":              row[11] or "replay",
        }
def main():
    log.info(f"Connecting to DB...")
    conn = psycopg2.connect(DB_URL)

    log.info("Fetching reading IDs...")
    ids = fetch_all_reading_ids(conn)
    total = len(ids)
    log.info(f"  {total:,} readings loaded — replay starts")

    client = build_mqtt_client()
    time.sleep(2)  # wait for MQTT connection

    idx      = 0
    cycle    = 0
    interval = INTERVAL_MS / 1000.0

    while True:
        esp32_active = (time.time() - last_esp32_message) < ESP32_TIMEOUT_S
        if esp32_active:
            log.info("Real ESP32 active — replay paused")
            while (time.time() - last_esp32_message) < ESP32_TIMEOUT_S:
                time.sleep(1)
            log.info("ESP32 went quiet — replay resuming")
        if idx >= total:
            idx    = 0
            cycle += 1
            log.info(f"Replay cycle {cycle} complete — restarting from beginning")

        reading_id = ids[idx]
        row = fetch_row(conn, reading_id)

        if row:
            payload = json.dumps(row)
            result  = client.publish(MQTT_TOPIC, payload, qos=1)

            if result.rc == mqtt.MQTT_ERR_SUCCESS:
                if idx % 50 == 0:
                    log.info(
                        f"[{idx+1}/{total}] cycle={cycle} "
                        f"device={row['device_id']} "
                        f"pA={row['node_a_pressure']:.0f} "
                        f"pB={row['node_b_pressure']:.0f} "
                        f"pC={row['node_c_pressure']:.0f}"
                    )
            else:
                log.warning(f"Publish failed rc={result.rc} — retrying next tick")

        idx += 1
        time.sleep(interval)


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        log.info("Replay simulator stopped")